# Git Objects

The git object model this project reads from and writes to `.git/objects`.

## Language

**Object type**:
One of the three kinds of object git stores: blob, tree, or commit.

**Mode**:
The code on a tree entry that says what kind of entry it is and its permissions, such as `100644` for a regular file or `40000` for a directory.
_Avoid_: permissions, file type

**Tree entry**:
One line of a tree: a mode, a name, and the SHA of the object it points to.
_Avoid_: record, row

**Gitlink**:
A tree entry with mode `160000` that points to a commit in another repository, used for submodules.
_Avoid_: submodule entry
