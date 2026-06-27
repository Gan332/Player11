import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

enum AppThemeMode { system, light, dark }

class ThemeProvider extends ChangeNotifier {
  AppThemeMode _themeMode = AppThemeMode.system;
  Color? _seedColor;
  bool _dynamicColorEnabled = true;

  AppThemeMode get appThemeMode => _themeMode;
  Color? get seedColor => _seedColor;
  bool get dynamicColorEnabled => _dynamicColorEnabled;

  ThemeMode get themeMode {
    switch (_themeMode) {
      case AppThemeMode.system:
        return ThemeMode.system;
      case AppThemeMode.light:
        return ThemeMode.light;
      case AppThemeMode.dark:
        return ThemeMode.dark;
    }
  }

  ThemeProvider() {
    _loadPreferences();
  }

  Future<void> _loadPreferences() async {
    final prefs = await SharedPreferences.getInstance();
    final modeIndex = prefs.getInt('themeMode') ?? 0;
    _themeMode = AppThemeMode.values[modeIndex];
    final colorValue = prefs.getInt('seedColor');
    _seedColor = colorValue != null ? Color(colorValue) : null;
    _dynamicColorEnabled = prefs.getBool('dynamicColorEnabled') ?? true;
    notifyListeners();
  }

  Future<void> setThemeMode(AppThemeMode mode) async {
    _themeMode = mode;
    notifyListeners();
    final prefs = await SharedPreferences.getInstance();
    await prefs.setInt('themeMode', mode.index);
  }

  Future<void> setSeedColor(Color? color) async {
    _seedColor = color;
    notifyListeners();
    final prefs = await SharedPreferences.getInstance();
    if (color != null) {
      await prefs.setInt('seedColor', color.value);
    } else {
      await prefs.remove('seedColor');
    }
  }

  Future<void> setDynamicColorEnabled(bool enabled) async {
    _dynamicColorEnabled = enabled;
    notifyListeners();
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('dynamicColorEnabled', enabled);
  }
}
