import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'dart:ui';

enum ThemeMode { system, light, dark }

class ThemeProvider extends ChangeNotifier {
  ThemeMode _themeMode = ThemeMode.system;
  Color? _seedColor;
  bool _dynamicColorEnabled = true;

  ThemeMode get themeMode => _themeMode;
  Color? get seedColor => _seedColor;
  bool get dynamicColorEnabled => _dynamicColorEnabled;

  ThemeProvider() {
    _loadPreferences();
  }

  Future<void> _loadPreferences() async {
    final prefs = await SharedPreferences.getInstance();
    final modeIndex = prefs.getInt('themeMode') ?? 0;
    _themeMode = ThemeMode.values[modeIndex];
    final colorValue = prefs.getInt('seedColor');
    _seedColor = colorValue != null ? Color(colorValue) : null;
    _dynamicColorEnabled = prefs.getBool('dynamicColorEnabled') ?? true;
    notifyListeners();
  }

  Future<void> setThemeMode(ThemeMode mode) async {
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

  FlutterThemeMode get flutterThemeMode {
    switch (_themeMode) {
      case ThemeMode.system:
        return FlutterThemeMode.system;
      case ThemeMode.light:
        return FlutterThemeMode.light;
      case ThemeMode.dark:
        return FlutterThemeMode.dark;
    }
  }
}
