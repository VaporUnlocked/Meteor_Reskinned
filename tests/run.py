#!/usr/bin/env python3
"""Headless regression checks. These do not launch Minecraft or validate GPU rendering."""
import argparse, importlib.util, json, os, shutil, subprocess
from pathlib import Path
from zipfile import ZipFile
ROOT=Path(__file__).resolve().parents[1]
spec=importlib.util.spec_from_file_location('reskin_build',ROOT/'build.py');build=importlib.util.module_from_spec(spec);spec.loader.exec_module(build)
from check_api import check

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--meteor',type=Path,required=True)
    p.add_argument('--addon',type=Path,default=ROOT/'dist'/'meteor-reskin-26.3-17-1.1.0-beta.1.jar')
    p.add_argument('--offline-java21-api-view',action='store_true')
    args=p.parse_args();args.meteor=args.meteor.resolve();args.addon=args.addon.resolve()
    javac=shutil.which('javac');java=shutil.which('java')
    if not javac or not java:p.error('JDK required')
    major=build.java_major(javac)
    api=args.meteor
    if major<25:
        if major!=21 or not args.offline_java21_api_view:p.error('Use JDK 25, or explicitly opt into the Java 21 test adapter')
        api=build.compatibility_view(api,ROOT/'build'/'test-meteor-api-java21.jar')
    output=ROOT/'build'/'regression';output.mkdir(parents=True,exist_ok=True)
    classes=output/'classes'
    if classes.exists():shutil.rmtree(classes)
    classes.mkdir()
    sources=sorted((ROOT/'tests'/'fixtures').rglob('*.java'))
    print('HEADLESS: game services, font metrics and draw submission are controlled test doubles. Not a game session.',flush=True)
    print('Meteor WContainer/WWindow/Cell/WPressable/Tab and Settings/Setting method bytecode is from the supplied artifact.',flush=True)
    subprocess.run([javac,'--release','21','-encoding','UTF-8','-cp',os.pathsep.join(map(str,[args.addon,api])),'-d',str(classes),*[str(x) for x in sources]],check=True)
    critical=['gui/widgets/containers/WContainer','gui/widgets/containers/WWindow','gui/utils/Cell','gui/widgets/containers/WView','gui/widgets/pressable/WPressable','gui/tabs/Tab']
    for name in critical:
        assert not (classes/('meteordevelopment/meteorclient/'+name+'.class')).exists(),'Lifecycle class must not be mocked'
    logs=[]
    def run(jar,mode):
        result=subprocess.run([java,'-Djava.awt.headless=true','-Xverify:all','-ea','-cp',os.pathsep.join(map(str,[classes,jar,api])), 'io.github.cloudycity.meteorreskin.ReskinRegression',mode],text=True,capture_output=True)
        print(result.stdout,end='');print(result.stderr,end='')
        logs.append(result.stdout+result.stderr)
        result.check_returncode()
    run(args.addon,'fixed')
    report=check(args.addon,args.meteor)
    report.update({'headless':True,'game_services_mocked':True,'java_major':major,'java21_class_header_adapter':major==21,'real_minecraft_runtime_test':False})
    (output/'results.txt').write_text('\n'.join(logs))
    (output/'api-report.json').write_text(json.dumps(report,indent=2)+'\n')
    print('PASS: archive and API checks; original Meteor artifact is not changed.')
if __name__=='__main__':main()
