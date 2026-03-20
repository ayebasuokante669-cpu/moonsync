import fs from 'fs';
import path from 'path';

function walk(dir) {
  let results = [];
  let list = fs.readdirSync(dir);
  list.forEach(function(file) {
    file = dir + '/' + file;
    let stat = fs.statSync(file);
    if (stat && stat.isDirectory()) { 
      results = results.concat(walk(file));
    } else { 
      results.push(file);
    }
  });
  return results;
}

const files = walk('./src/app').filter(f => f.endsWith('.jsx') || f.endsWith('.js'));

files.forEach(f => {
  let content = fs.readFileSync(f, 'utf8');
  let original = content;
  
  // simple replace
  content = content.replace(/className=(["`].*?)bg-white/g, 'className=$1bg-[var(--color-card)]');
  content = content.replace(/ className="([^"]*)bg-white/g, ' className="$1bg-[var(--color-card)]');
  content = content.replace(/ className="([^"]*)bg-white/g, ' className="$1bg-[var(--color-card)]');
  
  // Revert specific false positives that must stay white
  content = content.replace(/after:bg-\[var\(--color-card\)\]/g, 'after:bg-white');
  content = content.replace(/bg-\[var\(--color-card\)\]\/80/g, 'bg-[var(--color-card)]/80'); 
  content = content.replace(/bg-\[var\(--color-card\)\]\/50/g, 'bg-[var(--color-card)]/50'); 
  
  if (content !== original) {
    fs.writeFileSync(f, content, 'utf8');
    console.log('Fixed', f);
  }
});
