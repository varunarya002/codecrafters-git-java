#!/bin/sh
set -eu

repo_root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
temp_dir=$(mktemp -d)
trap 'rm -rf "$temp_dir"' EXIT

/usr/bin/git -C "$temp_dir" init -q
mkdir "$temp_dir/dir1" "$temp_dir/dir2"
printf 'regular\n' > "$temp_dir/file1"
printf '#!/bin/sh\n' > "$temp_dir/executable"
chmod +x "$temp_dir/executable"
printf 'nested\n' > "$temp_dir/dir1/nested"
printf 'other\n' > "$temp_dir/dir2/other"
ln -s file1 "$temp_dir/link"
/usr/bin/git -C "$temp_dir" add .
tree_sha=$(/usr/bin/git -C "$temp_dir" write-tree)
blob_sha=$(/usr/bin/git -C "$temp_dir" hash-object -w file1)

cd "$temp_dir"
/usr/bin/git ls-tree "$tree_sha" > expected
java --enable-preview -cp "$repo_root/target/classes" Main ls-tree "$tree_sha" 2>/dev/null > actual
diff -u expected actual

/usr/bin/git ls-tree --name-only "$tree_sha" > expected
java --enable-preview -cp "$repo_root/target/classes" Main ls-tree --name-only "$tree_sha" 2>/dev/null > actual
diff -u expected actual

if java --enable-preview -cp "$repo_root/target/classes" Main ls-tree "$blob_sha" > actual 2> error; then
    echo 'Expected blob to be rejected' >&2
    exit 1
fi
grep -q 'Not a tree object' error

if java --enable-preview -cp "$repo_root/target/classes" Main ls-tree --unknown "$tree_sha" > actual 2> error; then
    echo 'Expected unknown flag to be rejected' >&2
    exit 1
fi
grep -q 'Usage: ls-tree' error

if java --enable-preview -cp "$repo_root/target/classes" Main ls-tree invalid > actual 2> error; then
    echo 'Expected invalid SHA to be rejected' >&2
    exit 1
fi
grep -q 'Invalid SHA-1 hash' error
