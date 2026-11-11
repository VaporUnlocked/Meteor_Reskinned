#!/usr/bin/env python3
"""Resolve addon member references against the unchanged supplied Meteor class metadata."""
import argparse, json, struct
from pathlib import Path
from zipfile import ZipFile

class ClassInfo:
    def __init__(self,b):
        self.b=b;self.p=8
        self.cp=[None]*self.u2();i=1
        while i<len(self.cp):
            t=self.u1()
            if t==1:
                n=self.u2();self.cp[i]=(t,self.take(n).decode('utf-8',errors='replace'))
            elif t in (3,4): self.cp[i]=(t,self.take(4))
            elif t in (5,6):self.cp[i]=(t,self.take(8));i+=1
            elif t in (7,8,16,19,20):self.cp[i]=(t,self.u2())
            elif t in (9,10,11,12,17,18):self.cp[i]=(t,self.u2(),self.u2())
            elif t==15:self.cp[i]=(t,self.u1(),self.u2())
            else:raise ValueError(f'Unexpected constant pool tag {t}')
            i+=1
        self.access=self.u2();self.name=self.cls(self.u2());parent=self.u2()
        self.parents=([self.cls(parent)] if parent else [])+[self.cls(self.u2()) for _ in range(self.u2())]
        self.fields=self.members();self.methods=self.members()
    def take(self,n):r=self.b[self.p:self.p+n];self.p+=n;return r
    def u1(self):return int.from_bytes(self.take(1),'big')
    def u2(self):return int.from_bytes(self.take(2),'big')
    def u4(self):return int.from_bytes(self.take(4),'big')
    def utf(self,i):return self.cp[i][1]
    def cls(self,i):return self.utf(self.cp[i][1])
    def members(self):
        out=set()
        for _ in range(self.u2()):
            self.u2();out.add((self.utf(self.u2()),self.utf(self.u2())))
            for _ in range(self.u2()):self.u2();self.take(self.u4())
        return out
    def refs(self):
        for c in self.cp:
            if c and c[0] in (9,10,11):
                n=self.cp[c[2]]
                yield c[0],self.cls(c[1]),self.utf(n[1]),self.utf(n[2])

def check(addon,meteor):
    with ZipFile(addon) as az,ZipFile(meteor) as mz:
        assert az.testzip() is None and mz.testzip() is None
        sources=[ClassInfo(az.read(n)) for n in az.namelist() if n.endswith('.class')]
        assert all(c.name.startswith('io/github/cloudycity/meteorreskin/') for c in sources),'Foreign or test classes in release JAR'
        metadata=json.loads(az.read('fabric.mod.json'))
        assert metadata['version']=='1.1.0-beta.1'
        cache={c.name:c for c in sources}
        def load(n):
            if n not in cache:
                try:cache[n]=ClassInfo(mz.read(n+'.class'))
                except KeyError:return None
            return cache[n]
        def resolve(owner,name,desc,kind,seen=None):
            seen=set() if seen is None else seen
            if owner in seen:return False
            seen.add(owner);c=load(owner)
            if not c:
                if owner=='java/lang/Enum' and (name,desc)==('ordinal','()I'):return True
                return False
            if (name,desc) in (c.fields if kind==9 else c.methods):return True
            return name!='<init>' and any(resolve(p,name,desc,kind,seen) for p in c.parents)
        checked=set();external=set();errors=[]
        for c in sources:
            for kind,owner,name,desc in c.refs():
                if owner.startswith(('meteordevelopment/meteorclient/','io/github/cloudycity/meteorreskin/')):
                    ref=(kind,owner,name,desc);checked.add(ref)
                    if not resolve(*[owner,name,desc,kind]):
                        if name=='tick' and desc=='()V' and owner=='meteordevelopment/meteorclient/gui/tabs/TabScreen':
                            external.add(('net/minecraft/client/gui/screens/Screen',name,desc))
                            checked.discard(ref)
                        else:errors.append((c.name,ref))
                elif not owner.startswith(('java/','javax/')):external.add((owner,name,desc))
        if errors:raise AssertionError(json.dumps(errors,indent=2))
        return {'meteor_and_addon_member_references_resolved':len(checked),'compiled_addon_classes':len(sources),
                'external_references_not_resolved_by_this_check':sorted(external),'foreign_classes_in_jar':0}
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--addon',type=Path,required=True);p.add_argument('--meteor',type=Path,required=True);a=p.parse_args()
    print(json.dumps(check(a.addon,a.meteor),indent=2))
