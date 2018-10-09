IF "%2%"=="ADF" (
rd /s /q %ADF_DESTINATION%\%1
del /s /q /f %ADF_DESTINATION%\%1Errors.txt
del /s /q /f %ADF_DESTINATION%\%1Log.txt
)

IF "%2%"=="OAF" (
rd /s /q %OAF_SOURCE%\%1
del /s /q /f %ADF_DESTINATION%\%1\*.dtd
)