"""Regenerate the distributed tables from the downloaded Unicode Unihan ZIP.
Usage: python3 scripts/build_quick_dictionary.py /path/to/Unihan.zip
License: Unicode data remains subject to UNICODE-LICENSE.txt.
"""
import collections,re,sys,zipfile
from pathlib import Path
z=zipfile.ZipFile(sys.argv[1]);data=collections.defaultdict(dict)
for name in ['Unihan_DictionaryLikeData.txt','Unihan_Readings.txt','Unihan_Variants.txt']:
 for line in z.read(name).decode().splitlines():
  if line and not line.startswith('#'):
   p=line.split('\t')
   if len(p)==3:data[p[0]][p[1]]=p[2]
rows=[];trad=[]
for cp,d in data.items():
 if 'kTraditionalVariant' in d:
  targets=[c for c in re.findall(r'U\+[0-9A-F]+',d['kTraditionalVariant']) if c!=cp]
  if targets:trad.append(chr(int(cp[2:],16))+'\t'+chr(int(targets[0][2:],16)))
 if 'kCangjie' not in d:continue
 code=d['kCangjie'].lower()
 if not code.isascii() or not code.isalpha():continue
 if 'kTraditionalVariant' in d and cp not in d['kTraditionalVariant']:continue
 rows.append((int(d.get('kFrequency','6')),int(cp[2:],16),code if len(code)==1 else code[0]+code[-1],chr(int(cp[2:],16))))
rows.sort();out=[]
for rank,cp,code,ch in rows:
 out.append(code+'\t'+ch)
 if len(code)>1:out.append(code[0]+'\t'+ch)
root=Path(__file__).resolve().parents[1]/'app/assets';root.mkdir(exist_ok=True)
(root/'quick.tsv').write_text('\n'.join(out)+'\n')
(root/'traditional.tsv').write_text('\n'.join(trad)+'\n')
print('Unicode table characters:',len(rows))
