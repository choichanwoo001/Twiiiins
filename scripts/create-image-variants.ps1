param(
  [string]$UploadImageDir = "backend/uploads/image",
  [int[]]$Widths = @(640, 1280, 1920),
  [int]$Quality = 86,
  [switch]$Overwrite
)

$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Drawing

$sourceDir = Resolve-Path $UploadImageDir
$variantDir = Join-Path $sourceDir "variants"
New-Item -ItemType Directory -Force -Path $variantDir | Out-Null

$jpegCodec = [System.Drawing.Imaging.ImageCodecInfo]::GetImageEncoders() |
  Where-Object { $_.MimeType -eq "image/jpeg" }
$encoder = [System.Drawing.Imaging.Encoder]::Quality
$encoderParams = New-Object System.Drawing.Imaging.EncoderParameters(1)
$encoderParams.Param[0] = New-Object System.Drawing.Imaging.EncoderParameter($encoder, [int64]$Quality)

function Save-Variant {
  param(
    [string]$SourcePath,
    [string]$TargetPath,
    [int]$TargetWidth
  )

  $src = [System.Drawing.Image]::FromFile($SourcePath)
  try {
    $ratio = $TargetWidth / $src.Width
    $targetHeight = [int][Math]::Round($src.Height * $ratio)
    $bmp = New-Object System.Drawing.Bitmap($TargetWidth, $targetHeight)
    try {
      $graphics = [System.Drawing.Graphics]::FromImage($bmp)
      try {
        $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
        $graphics.Clear([System.Drawing.Color]::White)
        $graphics.DrawImage($src, 0, 0, $TargetWidth, $targetHeight)
      } finally {
        $graphics.Dispose()
      }
      $bmp.Save($TargetPath, $jpegCodec, $encoderParams)
    } finally {
      $bmp.Dispose()
    }
  } finally {
    $src.Dispose()
  }
}

$images = Get-ChildItem $sourceDir -File |
  Where-Object { $_.Extension -match "^\.(jpg|jpeg|png|webp)$" }

foreach ($image in $images) {
  foreach ($width in $Widths) {
    $targetPath = Join-Path $variantDir "$($image.BaseName)-$width.jpg"
    if ((Test-Path $targetPath) -and -not $Overwrite) {
      continue
    }
    Save-Variant -SourcePath $image.FullName -TargetPath $targetPath -TargetWidth $width
    Write-Host "Created $targetPath"
  }
}

Write-Host "Done. Processed $($images.Count) source image(s)."
