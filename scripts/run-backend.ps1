$envFile = Join-Path $PSScriptRoot "..\.env"

$env:DB_HOST = "localhost"
$env:DB_PORT = "5432"

Get-Content $envFile |
    Where-Object { $_ -match "^\s*[^#].*=" } |
    ForEach-Object {
        $name, $value = $_ -split "=", 2
        [Environment]::SetEnvironmentVariable(
            $name.Trim(),
            $value.Trim(),
            "Process"
        )
    }

Push-Location (Join-Path $PSScriptRoot "..\backend")
try {
    .\mvnw.cmd spring-boot:run
}
finally {
    Pop-Location
}