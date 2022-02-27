// (C) Copyright 2017, Google Inc.
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include "pango_font_info.h"
#include <pango/pango.h>
#include <cstdio>
#include <string>
#include "commandlineflags.h"
#include "fileio.h"
#include "gmock/gmock-matchers.h" // for EXPECT_THAT
#include "include_gunit.h"
#ifdef INCLUDE_TENSORFLOW
#  include "util/utf8/unicodetext.h" // for UnicodeText
#endif

namespace tesseract {

// Fonts in testdata directory
const char *kExpectedFontNames[] = {"Arab",
                                    "Arial Bold Italic",
                                    "DejaVu Sans Ultra-Light",
                                    "Lohit Hindi",
#if PANGO_VERSION <= 12005
                                    "Times New Roman",
#else
                                    "Times New Roman,", // Pango v1.36.2 requires a trailing ','
#endif
                                    "UnBatang",
                                    "Verdana"};

// Sample text used in tests.
const char kArabicText[] = "والفكر والصراع 1234,\nوالفكر والصراع";
const char kEngText[] = "the quick brown fox jumps over the lazy dog";
const char kHinText[] = "पिताने विवाह की | हो गई उद्विग्न वह सोचा";
const char kKorText[] = "이는 것으로";
// Hindi words containing illegal vowel sequences.
const char *kBadlyFormedHinWords[] = {
#if PANGO_VERSION <= 12005
    "उपयोक्ताो", "नहीें", "कहीअे", "पत्रिाका", "छह्णाीस",
#endif
    // Pango v1.36.2 will render the above words even though they are invalid.
    "प्रंात", nullptr};

static PangoFontMap *font_map;

class PangoFontInfoTest : public ::testing::Test {
protected:
  void SetUp() override {
    if (!font_map) {
      font_map = pango_cairo_font_map_new_for_font_type(CAIRO_FONT_TYPE_FT);
    }
    pango_cairo_font_map_set_default(PANGO_CAIRO_FONT_MAP(font_map));
  }

  // Creates a fake fonts.conf file that points to the testdata fonts for
  // fontconfig to initialize with.
  static void SetUpTestCase() {
    static std::locale system_locale("");
    std::locale::global(system_locale);

    FLAGS_fonts_dir = TESTING_DIR;
    FLAGS_fontconfig_tmpdir = FLAGS_test_tmpdir;
    file::MakeTmpdir();
    PangoFontInfo::SoftInitFontConfig(); // init early
  }

  PangoFontInfo font_info_;
};

TEST_F(PangoFontInfoTest, TestNonDefaultConstructor) {
  PangoFontInfo font("Arial Bold Italic 12");
  EXPECT_EQ(12, font.font_size());
  EXPECT_EQ("Arial", font.family_name());
}

TEST_F(PangoFontInfoTest, DoesParseFontDescriptionName) {
  EXPECT_TRUE(font_info_.ParseFontDescriptionName("Arial Bold Italic 12"));
  EXPECT_EQ(12, font_info_.font_size());
  EXPECT_EQ("Arial", font_info_.family_name());

  EXPECT_TRUE(font_info_.ParseFontDescriptionName("Verdana 10"));
  EXPECT_EQ(10, font_info_.font_size());
  EXPECT_EQ("Verdana", font_info_.family_name());

  EXPECT_TRUE(font_info_.ParseFontDescriptionName("DejaVu Sans Ultra-Light"));
  EXPECT_EQ("DejaVu Sans", font_info_.family_name());
}

TEST_F(PangoFontInfoTest, DoesParseMissingFonts) {
  // Font family one of whose faces exists but this one doesn't.
  EXPECT_TRUE(font_info_.ParseFontDescriptionName("Arial Italic 12"));
  EXPECT_EQ(12, font_info_.font_size());
  EXPECT_EQ("Arial", font_info_.family_name());

  // Font family that doesn't exist in testdata. It will still parse the
  // description name. But without the file, it will not be able to populate
  // some font family details, like is_monospace().
  EXPECT_TRUE(font_info_.ParseFontDescriptionName("Georgia 10"));
  EXPECT_EQ(10, font_info_.font_size());
  EXPECT_EQ("Georgia", font_info_.family_name());
}

TEST_F(PangoFontInfoTest, DoesGetSpacingProperties) {
  EXPECT_TRUE(font_info_.ParseFontDescriptionName("Arial Italic 12"));
  int x_bearing, x_advance;
  EXPECT_TRUE(font_info_.GetSpacingProperties("A", &x_bearing, &x_advance));
  EXPECT_GT(x_advance, 0);
  EXPECT_TRUE(font_info_.GetSpacingProperties("a", &x_bearing, &x_advance));
  EXPECT_GT(x_advance, 0);
}

TEST_F(PangoFontInfoTest, CanRenderString) {
  font_info_.ParseFontDescriptionName("Verdana 12");
  EXPECT_TRUE(font_info_.CanRenderString(kEngText, strlen(kEngText)));

  font_info_.ParseFontDescriptionName("UnBatang 12");
  EXPECT_TRUE(font_info_.CanRenderString(kKorText, strlen(kKorText)));

  font_info_.ParseFontDescriptionName("Lohit Hindi 12");
  EXPECT_TRUE(font_info_.CanRenderString(kHinText, strlen(kHinText)));
}

TEST_F(PangoFontInfoTest, CanRenderLigature) {
  font_info_.ParseFontDescriptionName("Arab 12");
  const char kArabicLigature[] = "لا";
  EXPECT_TRUE(font_info_.CanRenderString(kArabicLigature, strlen(kArabicLigature)));

  printf("Next word\n");
  EXPECT_TRUE(font_info_.CanRenderString(kArabicText, strlen(kArabicText)));
}

TEST_F(PangoFontInfoTest, CannotRenderUncoveredString) {
  font_info_.ParseFontDescriptionName("Verdana 12");
  EXPECT_FALSE(font_info_.CanRenderString(kKorText, strlen(kKorText)));
}

TEST_F(PangoFontInfoTest, CannotRenderInvalidString) {
  font_info_.ParseFontDescriptionName("Lohit Hindi 12");
  for (int i = 0; kBadlyFormedHinWords[i] != nullptr; ++i) {
    EXPECT_FALSE(
        font_info_.CanRenderString(kBadlyFormedHinWords[i], strlen(kBadlyFormedHinWords[i])))
        << "Can render " << kBadlyFormedHinWords[i];
  }
}

TEST_F(PangoFontInfoTest, CanDropUncoveredChars) {
  font_info_.ParseFontDescriptionName("Verdana 12");
  // Verdana cannot render the "ff" ligature
  std::string word = "oﬀice";
  EXPECT_EQ(1, font_info_.DropUncoveredChars(&word));
  EXPECT_EQ("oice", word);

  // Don't drop non-letter characters like word joiners.
  const char *kJoiners[] = {
      "\u2060", // U+2060 (WJ)
      "\u200C", // U+200C (ZWJ)
      "\u200D"  // U+200D (ZWNJ)
  };
  for (auto &kJoiner : kJoiners) {
    word = kJoiner;
    EXPECT_EQ(0, font_info_.DropUncoveredChars(&word));
    EXPECT_STREQ(kJoiner, word.c_str());
  }
}

// ------------------------ FontUtils ------------------------------------

class FontUtilsTest : public ::testing::Test {
protected:
  void SetUp() override {
    file::MakeTmpdir();
  }
  // Creates a fake fonts.conf file that points to the testdata fonts for
  // fontconfig to initialize with.
  static void SetUpTestCase() {
    FLAGS_fonts_dir = TESTING_DIR;
    FLAGS_fontconfig_tmpdir = FLAGS_test_tmpdir;
    if (!font_map) {
      font_map = pango_cairo_font_map_new_for_font_type(CAIRO_FONT_TYPE_FT);
    }
    pango_cairo_font_map_set_default(PANGO_CAIRO_FONT_MAP(font_map));
  }

#ifdef INCLUDE_TENSORFLOW
  void CountUnicodeChars(const char *utf8_text, std::unordered_map<char32, int64_t> *ch_map) {
    ch_map->clear();
    UnicodeText ut;
    ut.PointToUTF8(utf8_text, strlen(utf8_text));
    for (UnicodeText::const_iterator it = ut.begin(); it != ut.end(); ++it) {
#  if 0
      if (UnicodeProps::IsWhitespace(*it)) continue;
#  else
      if (std::isspace(*it))
        continue;
#  endif
      ++(*ch_map)[*it];
    }
  }
#endif
};

TEST_F(FontUtilsTest, DoesFindAvailableFonts) {
  EXPECT_TRUE(FontUtils::IsAvailableFont("Arial Bold Italic"));
  EXPECT_TRUE(FontUtils::IsAvailableFont("Verdana"));
  EXPECT_TRUE(FontUtils::IsAvailableFont("DejaVu Sans Ultra-Light"));

  // Test that we can support font name convention for Pango v1.30.2 even when
  // we are running an older version.
  EXPECT_TRUE(FontUtils::IsAvailableFont("Times New Roman,"));
}

TEST_F(FontUtilsTest, DoesDetectMissingFonts) {
  // Only bold italic face is available.
  EXPECT_FALSE(FontUtils::IsAvailableFont("Arial"));
  // Don't have a ttf for the Courier family.
  EXPECT_FALSE(FontUtils::IsAvailableFont("Courier"));
  // Pango "synthesizes" the italic font from the available Verdana Regular and
  // includes it in its list, but it is not really loadable.
  EXPECT_FALSE(FontUtils::IsAvailableFont("Verdana Italic"));
  // We have "Dejavu Sans Ultra-Light" but not its medium weight counterpart.
  EXPECT_FALSE(FontUtils::IsAvailableFont("DejaVu Sans"));
}

TEST_F(FontUtilsTest, DoesListAvailableFonts) {
  const std::vector<std::string> &fonts = FontUtils::ListAvailableFonts();
  EXPECT_THAT(fonts, ::testing::ElementsAreArray(kExpectedFontNames));
  for (auto &font : fonts) {
    PangoFontInfo font_info;
    EXPECT_TRUE(font_info.ParseFontDescriptionName(font));
  }
}

#ifdef INCLUDE_TENSORFLOW
TEST_F(FontUtilsTest, DoesFindBestFonts) {
  std::string fonts_list;
  std::unordered_map<char32, int64_t> ch_map;
  CountUnicodeChars(kEngText, &ch_map);
  EXPECT_EQ(26, ch_map.size()); // 26 letters
  std::vector<std::pair<const char *, std::vector<bool> > > font_flags;
  std::string best_list = FontUtils::BestFonts(ch_map, &font_flags);
  EXPECT_TRUE(best_list.size());
  // All fonts except Lohit Hindi should render English text.
  EXPECT_EQ(countof(kExpectedFontNames) - 1, font_flags.size());

  CountUnicodeChars(kKorText, &ch_map);
  best_list = FontUtils::BestFonts(ch_map, &font_flags);
  EXPECT_TRUE(best_list.size());
  // Only UnBatang font family is able to render korean.
  EXPECT_EQ(1, font_flags.size());
  EXPECT_STREQ("UnBatang", font_flags[0].first);
}
#endif

TEST_F(FontUtilsTest, DoesSelectFont) {
  const char *kLangText[] = {kArabicText, kEngText, kHinText, kKorText, nullptr};
  const char *kLangNames[] = {"Arabic", "English", "Hindi", "Korean", nullptr};
  for (int i = 0; kLangText[i] != nullptr; ++i) {
    SCOPED_TRACE(kLangNames[i]);
    std::vector<std::string> graphemes;
    std::string selected_font;
    EXPECT_TRUE(
        FontUtils::SelectFont(kLangText[i], strlen(kLangText[i]), &selected_font, &graphemes));
    EXPECT_TRUE(selected_font.size());
    EXPECT_TRUE(graphemes.size());
  }
}

TEST_F(FontUtilsTest, DoesFailToSelectFont) {
  const char kMixedScriptText[] = "पिताने विवाह की | والفكر والصراع";
  std::vector<std::string> graphemes;
  std::string selected_font;
  EXPECT_FALSE(FontUtils::SelectFont(kMixedScriptText, strlen(kMixedScriptText), &selected_font,
                                     &graphemes));
}

#if 0
// Needs fix. FontUtils::GetAllRenderableCharacters was removed
// because of deprecated pango_coverage_max.
TEST_F(FontUtilsTest, GetAllRenderableCharacters) {
  const int32_t kHindiChar = 0x0905;
  const int32_t kArabicChar = 0x0623;
  const int32_t kMongolianChar = 0x180E;  // Mongolian vowel separator
  const int32_t kOghamChar = 0x1680;      // Ogham space mark
  std::vector<bool> unicode_mask;
  FontUtils::GetAllRenderableCharacters(&unicode_mask);
  EXPECT_TRUE(unicode_mask['A']);
  EXPECT_TRUE(unicode_mask['1']);
  EXPECT_TRUE(unicode_mask[kHindiChar]);
  EXPECT_TRUE(unicode_mask[kArabicChar]);
  EXPECT_FALSE(unicode_mask[kMongolianChar]);  // no font for mongolian.
#  if 0 // TODO: check fails because DejaVu Sans Ultra-Light supports ogham
  EXPECT_FALSE(unicode_mask[kOghamChar]);      // no font for ogham.
#  endif
  unicode_mask.clear();

  std::vector<std::string> selected_fonts;
  selected_fonts.push_back("Lohit Hindi");
  FontUtils::GetAllRenderableCharacters(selected_fonts, &unicode_mask);
  EXPECT_TRUE(unicode_mask['1']);
  EXPECT_TRUE(unicode_mask[kHindiChar]);
  EXPECT_FALSE(unicode_mask['A']);             // Lohit doesn't render English,
  EXPECT_FALSE(unicode_mask[kArabicChar]);     // or Arabic,
  EXPECT_FALSE(unicode_mask[kMongolianChar]);  // or Mongolian,
  EXPECT_FALSE(unicode_mask[kOghamChar]);      // or Ogham.
  unicode_mask.clear();

  // Check that none of the included fonts cover the Mongolian or Ogham space
  // characters.
  for (size_t f = 0; f < countof(kExpectedFontNames); ++f) {
    std::string tracestring = "Testing " + kExpectedFontNames[f];
    SCOPED_TRACE(tracestring);
    FontUtils::GetAllRenderableCharacters(kExpectedFontNames[f], &unicode_mask);
#  if 0 // TODO: check fails because DejaVu Sans Ultra-Light supports ogham
    EXPECT_FALSE(unicode_mask[kOghamChar]);
#  endif
    EXPECT_FALSE(unicode_mask[kMongolianChar]);
    unicode_mask.clear();
  }
}
#endif

} // namespace tesseract
