@echo off
REM 使用 Sybase bcp 工具导出表数据为 CSV 文件
REM 请根据实际库名、表名、用户名、密码、服务器地址修改参数

set DB_SERVER=localhost
set DB_PORT=5000
set DB_NAME=your_db
set DB_USER=your_user
set DB_PASS=your_password
set TABLE_NAME=your_table
set OUT_FILE=table_snapshot.csv

REM 导出表数据
bcp %DB_NAME%.dbo.%TABLE_NAME% out %OUT_FILE% -c -t, -S %DB_SERVER%:%DB_PORT% -U %DB_USER% -P %DB_PASS%

REM 提示完成
if %ERRORLEVEL%==0 (
    echo 导出成功：%OUT_FILE%
) else (
    echo 导出失败，请检查参数和环境！
)
pause
