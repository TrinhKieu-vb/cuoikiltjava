@echo off
REM Start Client

echo Building project...
call mvn clean install -q

echo.
echo Starting Client...
mvn javafx:run -Djavafx.mainClass=org.example.client.MainApp

pause

