@echo off
REM Start Server

echo Building project...
call mvn clean install -q

echo.
echo Starting Server...
java -cp target/DrinkDessertShop-1.0-SNAPSHOT-jar-with-dependencies.jar org.example.server.Server

pause

