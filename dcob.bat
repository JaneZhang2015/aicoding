@echo off
REM 设置源文件夹和目标文件夹路径
set "SOURCE_DIR=C:\path\to\source"
set "DEST_DIR=C:\path\to\destination"
set "TEMP_DIR=%~dp0temp"

REM 创建临时文件夹
if not exist "%TEMP_DIR%" mkdir "%TEMP_DIR%"

REM 复制所有 .cbl 和 .cpy 文件到临时文件夹
copy "%SOURCE_DIR%\*.cbl" "%TEMP_DIR%"
copy "%SOURCE_DIR%\*.cpy" "%TEMP_DIR%"

REM 遍历所有 .cbl 文件并执行 dcoba 命令
for %%F in ("%TEMP_DIR%\*.cbl") do (
    dcoba "%%~nxF"
)

REM 复制所有生成的新文件到目标文件夹
xcopy "%TEMP_DIR%\*" "%DEST_DIR%\" /Y /S /I

REM 删除临时文件夹
rd /s /q "%TEMP_DIR%"

echo 完成！
pause