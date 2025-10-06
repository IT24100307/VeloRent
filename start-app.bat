@echo off
echo Starting VeloRent Car Rental System...
echo.
echo Navigating to project directory...
cd /d "c:\Users\chand\Desktop\VeloRent\Car-Rental-System"

echo Cleaning and compiling project...
call mvn clean compile -q

echo.
echo Starting Spring Boot application...
echo Application will be available at: http://localhost:9000
echo Press Ctrl+C to stop the application
echo.

java -cp "target/classes;target/dependency/*" Group2.Car.Rental.System.CarRentalSystemApplication

pause