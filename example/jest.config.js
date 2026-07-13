module.exports = {
  preset: '@react-native/jest-preset',
  transformIgnorePatterns: [
    'node_modules/(?!((@)?react-native|@react-native(-community)?|@react-navigation|react-native-screens|react-native-safe-area-context|react-native-stylus)/)',
  ],
};
