"""Lists client-only class references that a dedicated server would hit.

Scans the built jar with javap: for every class outside the client package, prints methods (not marked
@OnlyIn) whose bytecode or signature mentions net/minecraft/client or com/mojang/blaze3d classes.
"""
import glob, os, re, subprocess, sys, zipfile, tempfile

CLIENT = re.compile(r'(net/minecraft/client/[\w/$]+|com/mojang/blaze3d/[\w/$]+|rafradek/TF2weapons/client/[\w/$]+)')
jar = sorted(glob.glob('build/libs/*.jar'))[0]
tmp = tempfile.mkdtemp()
zipfile.ZipFile(jar).extractall(tmp)
classes = [c for c in glob.glob(tmp + '/rafradek/**/*.class', recursive=True) if '/TF2weapons/client/' not in c]
hits = 0
for i in range(0, len(classes), 200):
    out = subprocess.run(['javap', '-c', '-p', '-v'] + classes[i:i + 200], capture_output=True, text=True).stdout
    cls = None
    method = None
    onlyin = False
    refs = set()

    def flush():
        global hits
        if method and refs and not onlyin:
            hits += 1
            print(f'{cls} :: {method} -> {", ".join(sorted(refs))}')

    for line in out.splitlines():
        m = re.match(r'^(?:public |final |abstract |private |protected )*(?:class|interface|enum) (\S+)', line)
        if m:
            flush(); method = None; refs = set(); cls = m.group(1); continue
        m = re.match(r'^  (\S.*\);|\S.*[^;]\s\w+;)$', line)
        if m and not line.startswith('   '):
            flush(); method = m.group(1); onlyin = False; refs = set()
            for r in CLIENT.findall(line.replace('.', '/')):
                refs.add(r)
            continue
        if 'OnlyIn' in line:
            onlyin = True
        if method and ('//' in line or 'descriptor:' in line or 'Signature' in line):
            if 'invokestatic' in line and 'rafradek/TF2weapons/client' in line and 'Method' in line:
                # static calls into the client package are only resolved when executed
                pass
            else:
                for r in CLIENT.findall(line):
                    refs.add(r)
    flush()
print(f'TOTAL {hits}')
