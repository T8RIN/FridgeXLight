prefix=@CMAKE_INSTALL_PREFIX@
exec_prefix=${prefix}/bin
libdir=${prefix}/lib
includedir=${prefix}/include

Name: @tesseract_NAME@
Description: An OCR Engine that was developed at HP Labs between 1985 and 1995... and now at Google.
URL: https://github.com/tesseract-ocr/tesseract
Version: @tesseract_VERSION@
Libs: -L${libdir} -l@tesseract_OUTPUT_NAME@ @libarchive_LIBS@ @libcurl_LIBS@ @TENSORFLOW_LIBS@
Libs.private:
Cflags: -I${includedir}
