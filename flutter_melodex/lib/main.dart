import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:audio_service/audio_service.dart';
import 'providers/player_provider.dart';
import 'providers/library_provider.dart';
import 'providers/playlist_provider.dart';
import 'providers/theme_provider.dart';
import 'providers/stats_provider.dart';
import 'services/audio_handler.dart';
import 'theme/app_theme.dart';
import 'screens/main_screen.dart';

late AudioHandler audioHandler;

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  audioHandler = await AudioService.init(
    builder: () => MelodexAudioHandler(),
    config: const AudioServiceConfig(
      androidNotificationChannelId: 'com.musicplayer.melodex.channel.audio',
      androidNotificationChannelName: 'Melodex Playback',
      androidNotificationOngoing: true,
      androidStopForegroundOnPause: true,
    ),
  );

  runApp(const MelodexApp());
}

class MelodexApp extends StatelessWidget {
  const MelodexApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => ThemeProvider()),
        ChangeNotifierProvider(create: (_) => LibraryProvider()),
        ChangeNotifierProvider(create: (_) => PlayerProvider(audioHandler)),
        ChangeNotifierProvider(create: (_) => PlaylistProvider()),
        ChangeNotifierProvider(create: (_) => StatsProvider()),
      ],
      child: Consumer<ThemeProvider>(
        builder: (context, themeProvider, _) {
          return MaterialApp(
            title: 'Melodex',
            debugShowCheckedModeBanner: false,
            theme: AppTheme.lightTheme(themeProvider.seedColor),
            darkTheme: AppTheme.darkTheme(themeProvider.seedColor),
            themeMode: themeProvider.themeMode,
            home: const MainScreen(),
          );
        },
      ),
    );
  }
}
