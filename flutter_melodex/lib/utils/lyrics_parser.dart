import '../models/lyric_line.dart';

class LyricsParser {
  static List<LyricLine> parse(String lrcContent) {
    final lines = <LyricLine>[];
    final regex = RegExp(r'\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)');

    for (final line in lrcContent.split('\n')) {
      final matches = regex.allMatches(line);
      if (matches.isNotEmpty) {
        for (final match in matches) {
          final minutes = int.parse(match.group(1)!);
          final seconds = int.parse(match.group(2)!);
          final milliseconds = int.parse(match.group(3)!.padRight(3, '0'));
          final text = match.group(4)?.trim() ?? '';

          if (text.isNotEmpty) {
            final timeMs = minutes * 60000 + seconds * 1000 + milliseconds;
            lines.add(LyricLine(timeMs: timeMs, text: text));
          }
        }
      }
    }

    lines.sort((a, b) => a.timeMs.compareTo(b.timeMs));
    return lines;
  }

  static int getCurrentLineIndex(List<LyricLine> lyrics, int positionMs) {
    for (int i = lyrics.length - 1; i >= 0; i--) {
      if (lyrics[i].timeMs <= positionMs) {
        return i;
      }
    }
    return 0;
  }
}
