# Local Bluetooth Unicode receiver. No network listener, no text logs.
Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing
$receiverSource = Join-Path $PSScriptRoot 'KeyboardReceiver.cs'
Add-Type -Path $receiverSource -ReferencedAssemblies System.Windows.Forms,System.Drawing
$mutex = New-Object System.Threading.Mutex($false, 'Local\AirPointerKeyboardReceiverV1')
if (-not $mutex.WaitOne(0, $false)) { Write-Host 'AirPointer keyboard receiver is already running.'; exit }
try { [System.Windows.Forms.Application]::Run((New-Object AirPointerReceiver)) }
finally { $mutex.ReleaseMutex(); $mutex.Dispose() }
