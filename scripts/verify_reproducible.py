#!/usr/bin/env python3
"""Build twice in different directories; compare release and source archive bytes."""
import hashlib
import json
from pathlib import Path
import shutil
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def main():
    output = ROOT / 'build' / 'libs'
    reports = ROOT / 'build' / 'reports'
    with tempfile.TemporaryDirectory(prefix='ipn-repro-') as tmp:
        builds = []
        for label in ('first-path', 'different-second-path'):
            target = Path(tmp) / label
            shutil.copytree(ROOT, target, ignore=shutil.ignore_patterns('.git', '.gradle', '.kotlin', 'build', '__pycache__'))
            (target / 'gradlew').chmod(0o755)
            subprocess.run(['./gradlew', '--no-daemon', '--no-build-cache', '--rerun-tasks', 'clean', 'build'], cwd=target, check=True)
            artifacts = sorted((target / 'build' / 'libs').glob('*.jar'))
            if len(artifacts) != 2:
                raise RuntimeError(f'Expected release and sources JARs; found {artifacts}')
            builds.append((target, {f.name: digest(f) for f in artifacts}))
        if builds[0][1] != builds[1][1]:
            raise RuntimeError('Non-reproducible artifacts: ' + json.dumps([b[1] for b in builds], indent=2))
        output.mkdir(parents=True, exist_ok=True)
        reports.mkdir(parents=True, exist_ok=True)
        for name in builds[0][1]:
            shutil.copy2(builds[0][0] / 'build' / 'libs' / name, output / name)
        shutil.copy2(builds[0][0] / 'build' / 'reports' / 'release-verification.json', reports / 'release-verification.json')
        (output / 'SHA256SUMS').write_text(''.join(f'{sha}  {name}\n' for name, sha in builds[0][1].items()))
        (reports / 'reproducibility.json').write_text(json.dumps({
            'status': 'passed', 'independent_builds': 2,
            'different_absolute_paths': True, 'build_cache': False,
            'sha256': builds[0][1], 'gameplay_tested': False,
            'scope': 'Same machine, JDK and dependencies; not cross-platform reproducibility'
        }, indent=2) + '\n')
        print('Two isolated builds produced byte-identical release and source JARs.')

if __name__ == '__main__':
    main()
