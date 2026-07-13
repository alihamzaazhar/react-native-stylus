import {StyleSheet} from 'react-native';
export const colors = {paper: '#f4f0e7', ink: '#17211b', moss: '#365d45', rust: '#b54b2a', card: '#fffdf7', line: '#d6cdbd', muted: '#6f766f'};
export const styles = StyleSheet.create({
  screen: {flex: 1, backgroundColor: colors.paper}, content: {padding: 18, gap: 14},
  eyebrow: {fontSize: 12, letterSpacing: 2, color: colors.rust, fontWeight: '800'},
  title: {fontSize: 30, lineHeight: 34, color: colors.ink, fontWeight: '900'},
  body: {fontSize: 15, lineHeight: 22, color: colors.muted},
  card: {backgroundColor: colors.card, borderWidth: 1, borderColor: colors.line, borderRadius: 16, padding: 16, gap: 8},
  row: {flexDirection: 'row', gap: 8, flexWrap: 'wrap'},
  button: {backgroundColor: colors.ink, borderRadius: 10, paddingHorizontal: 14, paddingVertical: 10},
  buttonAlt: {backgroundColor: colors.moss}, buttonText: {color: '#fff', fontWeight: '800'},
  metric: {fontFamily: 'monospace', color: colors.ink, fontSize: 13},
  canvas: {height: 390, backgroundColor: '#fff', borderWidth: 1, borderColor: colors.line, borderRadius: 16, overflow: 'hidden'},
  input: {height: 70, borderWidth: 1, borderColor: colors.line, borderRadius: 12, backgroundColor: '#fff'},
});
