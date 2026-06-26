$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$depsDir = Join-Path $projectRoot ".deps"
$buildDir = Join-Path $projectRoot "build"
$classesDir = Join-Path $buildDir "classes"
$jarPath = Join-Path $buildDir "FocusCorreio.jar"
$apiJar = Join-Path $depsDir "paper-api-1.20.6.jar"
$paperBaseUrl = "https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/1.20.6-R0.1-SNAPSHOT"
$metadataUrl = "$paperBaseUrl/maven-metadata.xml"
$compileDeps = @(
    @{
        Path = Join-Path $depsDir "annotations-24.1.0.jar"
        Url = "https://repo1.maven.org/maven2/org/jetbrains/annotations/24.1.0/annotations-24.1.0.jar"
    },
    @{
        Path = Join-Path $depsDir "checker-qual-3.33.0.jar"
        Url = "https://repo1.maven.org/maven2/org/checkerframework/checker-qual/3.33.0/checker-qual-3.33.0.jar"
    },
    @{
        Path = Join-Path $depsDir "guava-32.1.2-jre.jar"
        Url = "https://repo1.maven.org/maven2/com/google/guava/guava/32.1.2-jre/guava-32.1.2-jre.jar"
    },
    @{
        Path = Join-Path $depsDir "gson-2.10.1.jar"
        Url = "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar"
    },
    @{
        Path = Join-Path $depsDir "adventure-api-4.17.0.jar"
        Url = "https://repo1.maven.org/maven2/net/kyori/adventure-api/4.17.0/adventure-api-4.17.0.jar"
    },
    @{
        Path = Join-Path $depsDir "adventure-key-4.17.0.jar"
        Url = "https://repo1.maven.org/maven2/net/kyori/adventure-key/4.17.0/adventure-key-4.17.0.jar"
    },
    @{
        Path = Join-Path $depsDir "examination-api-1.3.0.jar"
        Url = "https://repo1.maven.org/maven2/net/kyori/examination-api/1.3.0/examination-api-1.3.0.jar"
    },
    @{
        Path = Join-Path $depsDir "examination-string-1.3.0.jar"
        Url = "https://repo1.maven.org/maven2/net/kyori/examination-string/1.3.0/examination-string-1.3.0.jar"
    },
    @{
        Path = Join-Path $depsDir "bungeecord-chat-1.20-R0.2-deprecated+build.18.jar"
        Url = "https://repo.papermc.io/repository/maven-public/net/md-5/bungeecord-chat/1.20-R0.2-deprecated+build.18/bungeecord-chat-1.20-R0.2-deprecated+build.18.jar"
    }
)

New-Item -ItemType Directory -Force -Path $depsDir, $classesDir | Out-Null

if (!(Test-Path $apiJar)) {
    Write-Host "Baixando Paper API..."
    [xml]$metadata = (Invoke-WebRequest -Uri $metadataUrl -UseBasicParsing).Content
    $snapshot = $metadata.metadata.versioning.snapshotVersions.snapshotVersion |
        Where-Object { $_.extension -eq "jar" -and [string]::IsNullOrEmpty($_.classifier) } |
        Select-Object -First 1

    if ($null -eq $snapshot) {
        throw "Nao foi possivel encontrar a snapshot jar da Paper API."
    }

    $apiUrl = "$paperBaseUrl/paper-api-$($snapshot.value).jar"
    Invoke-WebRequest -Uri $apiUrl -OutFile $apiJar
}

foreach ($dependency in $compileDeps) {
    if (!(Test-Path $dependency.Path)) {
        Write-Host "Baixando $([System.IO.Path]::GetFileName($dependency.Path))..."
        Invoke-WebRequest -Uri $dependency.Url -OutFile $dependency.Path
    }
}

if (Test-Path $jarPath) {
    Remove-Item -LiteralPath $jarPath -Force
}

Get-ChildItem -LiteralPath $classesDir -Recurse -Force | Remove-Item -Recurse -Force

$sources = Get-ChildItem -LiteralPath (Join-Path $projectRoot "src/main/java") -Recurse -Filter *.java | ForEach-Object { $_.FullName }
if ($sources.Count -eq 0) {
    throw "Nenhum arquivo Java encontrado."
}

Write-Host "Compilando..."
$dependencyJars = $compileDeps | ForEach-Object { $_.Path }
$classPathEntries = @($apiJar) + $dependencyJars
$classPath = $classPathEntries -join [System.IO.Path]::PathSeparator
& javac --release 17 -encoding UTF-8 -cp $classPath -d $classesDir @sources
if ($LASTEXITCODE -ne 0) {
    throw "Falha na compilacao."
}

Write-Host "Copiando recursos..."
Copy-Item -Path (Join-Path $projectRoot "src/main/resources/*") -Destination $classesDir -Recurse -Force

Write-Host "Gerando jar..."
Push-Location $classesDir
try {
    & jar --create --file $jarPath .
    if ($LASTEXITCODE -ne 0) {
        throw "Falha ao gerar o jar."
    }
}
finally {
    Pop-Location
}

Write-Host "Pronto: $jarPath"
