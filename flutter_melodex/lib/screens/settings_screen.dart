import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/theme_provider.dart';
import 'stats_screen.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Settings'),
      ),
      body: ListView(
        padding: const EdgeInsets.only(bottom: 80),
        children: [
          _buildThemeSection(context),
          const Divider(),
          _buildStatsLink(context),
          const Divider(),
          _buildAboutSection(context),
        ],
      ),
    );
  }

  Widget _buildThemeSection(BuildContext context) {
    return Consumer<ThemeProvider>(
      builder: (context, themeProvider, _) {
        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
              child: Text(
                'Theme',
                style: Theme.of(context).textTheme.titleMedium?.copyWith(
                  color: Theme.of(context).colorScheme.primary,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: Wrap(
                spacing: 8,
                children: [
                  ChoiceChip(
                    label: const Text('System'),
                    selected: themeProvider.themeMode == ThemeMode.system,
                    onSelected: (selected) {
                      if (selected) themeProvider.setThemeMode(ThemeMode.system);
                    },
                  ),
                  ChoiceChip(
                    label: const Text('Light'),
                    selected: themeProvider.themeMode == ThemeMode.light,
                    onSelected: (selected) {
                      if (selected) themeProvider.setThemeMode(ThemeMode.light);
                    },
                  ),
                  ChoiceChip(
                    label: const Text('Dark'),
                    selected: themeProvider.themeMode == ThemeMode.dark,
                    onSelected: (selected) {
                      if (selected) themeProvider.setThemeMode(ThemeMode.dark);
                    },
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),
            ListTile(
              leading: const Icon(Icons.palette_outlined),
              title: const Text('Seed Color'),
              subtitle: const Text('Customize accent color'),
              trailing: Container(
                width: 32,
                height: 32,
                decoration: BoxDecoration(
                  color: themeProvider.seedColor ?? Theme.of(context).colorScheme.primary,
                  shape: BoxShape.circle,
                ),
              ),
              onTap: () {
                _showColorPicker(context, themeProvider);
              },
            ),
          ],
        );
      },
    );
  }

  Widget _buildStatsLink(BuildContext context) {
    return ListTile(
      leading: const Icon(Icons.bar_chart_outlined),
      title: const Text('Listening Statistics'),
      subtitle: const Text('View your play history'),
      trailing: const Icon(Icons.chevron_right),
      onTap: () {
        Navigator.push(
          context,
          MaterialPageRoute(builder: (_) => const StatsScreen()),
        );
      },
    );
  }

  Widget _buildAboutSection(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
          child: Text(
            'About',
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
              color: Theme.of(context).colorScheme.primary,
              fontWeight: FontWeight.bold,
            ),
          ),
        ),
        const ListTile(
          leading: Icon(Icons.info_outline),
          title: Text('Melodex'),
          subtitle: Text('A Material Design 3 music player'),
        ),
        const ListTile(
          leading: Icon(Icons.update),
          title: Text('Version'),
          subtitle: Text('1.0.0'),
        ),
      ],
    );
  }

  void _showColorPicker(BuildContext context, ThemeProvider themeProvider) {
    final colors = [
      const Color(0xFF6750A4),
      const Color(0xFF006874),
      const Color(0xFF006C4C),
      const Color(0xFF7D5260),
      const Color(0xFF0061A4),
      const Color(0xFF476810),
      const Color(0xFF904D00),
      const Color(0xFFBA1A1A),
      const Color(0xFF1A6B52),
      const Color(0xFF4355B9),
    ];

    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('Choose Seed Color'),
          content: Wrap(
            spacing: 12,
            runSpacing: 12,
            children: colors.map((color) {
              final isSelected = themeProvider.seedColor?.value == color.value;
              return GestureDetector(
                onTap: () {
                  themeProvider.setSeedColor(color);
                  Navigator.pop(context);
                },
                child: Container(
                  width: 48,
                  height: 48,
                  decoration: BoxDecoration(
                    color: color,
                    shape: BoxShape.circle,
                    border: isSelected
                        ? Border.all(
                            color: Theme.of(context).colorScheme.onSurface,
                            width: 3,
                          )
                        : null,
                  ),
                  child: isSelected
                      ? const Icon(Icons.check, color: Colors.white)
                      : null,
                ),
              );
            }).toList(),
          ),
          actions: [
            TextButton(
              onPressed: () {
                themeProvider.setSeedColor(null);
                Navigator.pop(context);
              },
              child: const Text('Reset to Default'),
            ),
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Cancel'),
            ),
          ],
        );
      },
    );
  }
}
