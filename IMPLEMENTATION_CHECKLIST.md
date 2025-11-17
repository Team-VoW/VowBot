# Audio Conversion Implementation Checklist

## ✅ Completed Tasks

- [x] Added FFmpeg Java wrapper dependency to `build.gradle`
- [x] Created `AudioConversionService.java` with MP3 conversion logic
- [x] Updated `DiscordPollHandler.java` to use audio conversion
- [x] Modified `Dockerfile` to install FFmpeg
- [x] Created comprehensive documentation (`AUDIO_CONVERSION_README.md`)
- [x] Implemented error handling and fallback mechanisms
- [x] Added automatic file cleanup

## 📋 Setup Checklist (For You)

### Local Development Setup
- [ ] Download FFmpeg for Windows from https://www.gyan.dev/ffmpeg/builds/
- [ ] Extract FFmpeg to a folder (e.g., `C:\ffmpeg`)
- [ ] Add FFmpeg's `bin` folder to System PATH
- [ ] Restart IDE/terminal
- [ ] Verify installation: `ffmpeg -version`

### Build & Test
- [ ] Run `.\gradlew.bat clean build` (requires internet for first time)
- [ ] Check for any compilation errors
- [ ] Deploy to test environment
- [ ] Test with real audition files
- [ ] Verify file sizes are reduced
- [ ] Check Discord uploads work correctly

### Docker Deployment
- [ ] Rebuild Docker image (FFmpeg will be auto-installed)
- [ ] Test in Docker environment
- [ ] Verify logs show conversion activity

## 🔍 Testing Scenarios

1. **Normal Case:** Upload various audio formats (.ogg, .wav, .m4a)
   - Expected: All converted to MP3, smaller file size

2. **Already MP3:** Upload a small MP3 file
   - Expected: Skip conversion, use original

3. **Large File:** Upload a large audio file
   - Expected: Converted to MP3, significantly reduced size

4. **FFmpeg Missing:** Run without FFmpeg installed
   - Expected: Warning in logs, uses original file (graceful fallback)

## 📊 Key Features

### Supported Audio Formats
The bot now accepts these file types for auditions:
- **Compressed:** `.mp3`, `.m4a`, `.aac`, `.ogg`, `.opus`, `.flac`, `.wma`
- **Uncompressed:** `.wav`, `.aiff`, `.alac`
- **Video containers:** `.mp4`, `.mov`, `.webm` (extracts audio track)

All formats are automatically converted to MP3 for consistent delivery.

### Conversion Settings
- **Bitrate:** 128kbps (configurable)
- **Channels:** Mono (1 channel for voice)
- **Sample Rate:** 44.1kHz
- **Codec:** libmp3lame (high quality MP3 encoder)

### Smart Optimization
- Skips conversion if already MP3 and under 7MB
- Deletes original after successful conversion
- Falls back to original if conversion fails

### File Size Reduction Examples
- WAV file (10MB) → MP3 (~1-2MB) ✅
- OGG file (5MB) → MP3 (~600KB) ✅
- M4A file (3MB) → MP3 (~400KB) ✅

## 🚨 Troubleshooting

### If build fails:
1. Check internet connection (needed to download dependencies)
2. Try: `.\gradlew.bat clean build --refresh-dependencies`
3. Check Gradle version compatibility

### If conversion fails at runtime:
1. Verify FFmpeg is installed: `ffmpeg -version`
2. Check FFmpeg is in PATH
3. Look for error messages in bot logs
4. Bot will automatically fall back to original file

### If files still too large:
1. Reduce bitrate in `AudioConversionService.java`
2. Change `setAudioBitRate(128_000)` to `setAudioBitRate(96_000)`
3. Or reduce sample rate to 22050

## 📝 Configuration Options

Edit `AudioConversionService.java` to customize:

```java
.setAudioBitRate(128_000)     // Quality: 96k-192k
.setAudioChannels(1)          // 1=Mono, 2=Stereo
.setAudioSampleRate(44_100)   // 22050, 44100, or 48000
```

## 🎯 Expected Outcomes

✅ Audition files automatically converted to MP3  
✅ File sizes reduced by 60-90%  
✅ Faster Discord uploads  
✅ No manual intervention required  
✅ Graceful handling of errors  
✅ All files in consistent format  

## 📚 Documentation

- `AUDIO_CONVERSION_README.md` - Complete feature documentation
- `src/main/java/me/kmaxi/wynnvp/services/AudioConversionService.java` - Well-commented code
- Inline comments in `DiscordPollHandler.java`

---

**Status:** Implementation complete, ready for testing after FFmpeg setup

