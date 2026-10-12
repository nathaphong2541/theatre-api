@echo off
REM =====================================================================
REM  Deploy the Thai Theatre API to the GCP VM (docker container).
REM  One-shot: build jar -> upload jar + properties -> restart container.
REM  Run from the thaitheatre-api folder:  deploy-api.bat
REM =====================================================================
setlocal

set PROJECT=avian-augury-483807-g3
set ZONE=asia-southeast1-b
set VM=thaitheatre-server
set SSH_USER=nathaphong.t
set UPLOAD_DIR=/home/%SSH_USER%/api-upload
set APP_DIR=/opt/thaitheatre/api
set CONTAINER=thaitheatre-api
set JAR=target\api-0.0.1-SNAPSHOT.jar

echo.
echo === [1/5] Building jar (skip tests) ===
call mvnw.cmd -q -DskipTests clean package
if errorlevel 1 (
  echo !! Maven build failed. Aborting deploy.
  exit /b 1
)
if not exist "%JAR%" (
  echo !! Jar not found at %JAR% . Aborting.
  exit /b 1
)

echo.
echo === [2/5] Preparing upload folder on the VM ===
call gcloud compute ssh %VM% --project=%PROJECT% --zone=%ZONE% --tunnel-through-iap --command="rm -rf %UPLOAD_DIR% && mkdir -p %UPLOAD_DIR%"
if errorlevel 1 (
  echo !! Could not prepare upload folder on the VM. Aborting.
  exit /b 1
)

echo.
echo === [3/5] Uploading jar + application.properties ===
call gcloud compute scp "%JAR%" %VM%:%UPLOAD_DIR%/app.jar --project=%PROJECT% --zone=%ZONE% --tunnel-through-iap
if errorlevel 1 (
  echo !! Jar upload failed. Aborting.
  exit /b 1
)
call gcloud compute scp "src\main\resources\application.properties" %VM%:%UPLOAD_DIR%/application.properties --project=%PROJECT% --zone=%ZONE% --tunnel-through-iap
if errorlevel 1 (
  echo !! properties upload failed. Aborting.
  exit /b 1
)

echo.
echo === [4/5] Publishing into %APP_DIR% and restarting the container ===
call gcloud compute ssh %VM% --project=%PROJECT% --zone=%ZONE% --tunnel-through-iap --command="sudo mkdir -p %APP_DIR% && sudo cp %UPLOAD_DIR%/app.jar %APP_DIR%/app.jar && sudo cp %UPLOAD_DIR%/application.properties %APP_DIR%/application.properties && sudo docker restart %CONTAINER% && echo RESTARTED"
if errorlevel 1 (
  echo !! Publish/restart failed.
  exit /b 1
)

echo.
echo === [5/5] Tailing startup log (Ctrl+C to stop) ===
echo Waiting 8s for the app to boot...
timeout /t 8 /nobreak >nul
REM This is informational only. A non-zero exit from "docker logs" / the ssh
REM session closing must NOT mark the deploy as failed -- publish+restart
REM already succeeded in step 4. Swallow its exit code.
call gcloud compute ssh %VM% --project=%PROJECT% --zone=%ZONE% --tunnel-through-iap --command="sudo docker logs --tail 40 %CONTAINER%" || echo (log tail returned non-zero; ignoring)

echo.
echo =====================================================================
echo  API deployed. Health check:  http://34.21.164.108/api  (via nginx)
echo  If the log shows a DB connection timeout, the API cannot reach
echo  its database yet (separate issue from this deploy).
echo =====================================================================
endlocal
REM Reaching here means build + upload + publish + restart all succeeded.
exit /b 0
