const fs = require('fs');
const path = require('path');

function walkDir(dir, callback) {
  fs.readdirSync(dir).forEach(f => {
    let dirPath = path.join(dir, f);
    let isDirectory = fs.statSync(dirPath).isDirectory();
    isDirectory ? walkDir(dirPath, callback) : callback(path.join(dir, f));
  });
}

walkDir('./src/app', function(filePath) {
  if (filePath.endsWith('.jsx') || filePath.endsWith('.js')) {
    let content = fs.readFileSync(filePath, 'utf8');
    
    // We want to replace "bg-white" but NOT "after:bg-white" and NOT "bg-white/80" (unless we want to?)
    // Actually, bg-white/50 or bg-white/80 should probably become bg-[var(--color-card)]/50
    // Let's use a regex to replace (\s|"|'|`)bg-white(\b) but allow opacity like bg-white/50
    // Let's replace ONLY specifically "bg-white" preceded by whitespace or quote and followed by space, quote, or slash
    
    let original = content;
    
    // Replace standard bg-white
    content = content.replace(/(?<=[\s"'`])bg-white(?=[\s"'`\/\\])/g, 'bg-[var(--color-card)]');
    
    // Revert cases where it was after:bg-white or before:bg-white
    // Regex above uses lookbehind for whitespace/quotes, so after:bg-white wouldn't match anyway because it's preceded by ':'
    // What about text-white? Not changed.
    
    if (content !== original) {
      fs.writeFileSync(filePath, content, 'utf8');
      console.log('Updated:', filePath);
    }
  }
});
