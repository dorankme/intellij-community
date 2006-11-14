package com.intellij.localvcs;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ChangesTest extends TestCase {
  private RootEntry root;

  @Before
  public void setUp() {
    root = new RootEntry();
  }

  @Test
  public void testAffectedEntryIdForCreateFileChange() {
    root.doCreateDirectory(99, p("dir"));
    Change c = new CreateFileChange(1, p("dir/name"), null);
    c.applyTo(root);

    assertElements(new IdPath[]{idp(99, 1)}, c.getAffectedEntryIdPaths());

    assertTrue(c.affects(root.getEntry(1)));
    assertTrue(c.affects(root.getEntry(99)));
  }

  @Test
  public void testCollectingDifferencesForCreateFileChange() {
    Change c = new CreateFileChange(1, p("file"), "content");
    c.applyTo(root);

    root.doRename(p("file"), "new file");
    root.doChangeFileContent(p("new file"), "new content");

    List<Difference> diffs =
        c.getDifferences(root, root.getEntry(p("new file")));

    assertEquals(1, diffs.size());
    assertTrue(diffs.get(0).isCreated());

    assertEquals("file", diffs.get(0).getOlderEntry().getName());
    assertEquals("content", diffs.get(0).getOlderEntry().getContent());

    assertEquals("new file", diffs.get(0).getCurrentEntry().getName());
    assertEquals("new content", diffs.get(0).getCurrentEntry().getContent());
  }

  @Test
  public void testCollectingDifferencesForCreateFileChangeForUnaffectedFile() {
    Change c = new CreateFileChange(1, p("file"), null);
    c.applyTo(root);
    root.doCreateFile(2, p("another file"), null);

    List<Difference> d =
        c.getDifferences(root, root.getEntry(p("another file")));
    assertTrue(d.isEmpty());
  }

  @Test
  public void testCollectingDifferencesForCreateFileChangeInDirectory() {
    root.doCreateDirectory(1, p("dir"));

    Change c = new CreateFileChange(2, p("dir/file"), null);
    c.applyTo(root);

    List<Difference> diffs = c.getDifferences(root, root.getEntry(p("dir")));
    assertEquals(1, diffs.size());
    assertTrue(diffs.get(0).isCreated());

    assertEquals("file", diffs.get(0).getOlderEntry().getName());
    assertEquals("file", diffs.get(0).getCurrentEntry().getName());
  }

  @Test
  public void testAffectedEntryForCreateDirectoryChange() {
    Change c = new CreateDirectoryChange(2, p("name"));
    c.applyTo(root);

    assertElements(new IdPath[]{idp(2)}, c.getAffectedEntryIdPaths());
  }

  @Test
  public void testCollectingDifferencesForDirectoryFileChange() {
    root.doCreateDirectory(1, p("dir1"));
    root.doCreateDirectory(2, p("dir2"));

    Change c = new CreateDirectoryChange(3, p("dir2/dir3"));
    c.applyTo(root);

    List<Difference> d1 = c.getDifferences(root, root.getEntry(p("dir1")));
    assertTrue(d1.isEmpty());

    List<Difference> d2 = c.getDifferences(root, root.getEntry(p("dir2")));
    assertEquals(1, d2.size());
    assertTrue(d2.get(0).isCreated());

    List<Difference> d3 = c.getDifferences(root, root.getEntry(p("dir2/dir3")));
    assertEquals(1, d3.size());
    assertTrue(d3.get(0).isCreated());
  }

  @Test
  public void testAffectedEntryForChangeFileContentChange() {
    root.doCreateFile(16, p("file"), "content");

    Change c = new ChangeFileContentChange(p("file"), "new content");
    c.applyTo(root);

    assertElements(new IdPath[]{idp(16)}, c.getAffectedEntryIdPaths());
  }

  @Test
  public void testCollectingDifferencesForFileContentChange() {
    root.doCreateDirectory(1, p("dir"));
    root.doCreateFile(2, p("dir/file"), "a");

    Change c = new ChangeFileContentChange(p("dir/file"), "b");
    c.applyTo(root);

    root.doRename(p("dir/file"), "new file");
    root.doChangeFileContent(p("dir/new file"), "c");

    List<Difference> d1 =
        c.getDifferences(root, root.getEntry(p("dir/new file")));
    assertEquals(1, d1.size());
    assertTrue(d1.get(0).isModified());

    assertEquals("file", d1.get(0).getOlderEntry().getName());
    assertEquals("b", d1.get(0).getOlderEntry().getContent());

    assertEquals("new file", d1.get(0).getCurrentEntry().getName());
    assertEquals("c", d1.get(0).getCurrentEntry().getContent());

    List<Difference> d2 = c.getDifferences(root, root.getEntry(p("dir")));
    assertEquals(1, d2.size());
    assertTrue(d2.get(0).isModified());

    assertEquals("file", d2.get(0).getOlderEntry().getName());
    assertEquals("b", d2.get(0).getOlderEntry().getContent());

    assertEquals("new file", d2.get(0).getCurrentEntry().getName());
    assertEquals("c", d2.get(0).getCurrentEntry().getContent());
  }

  @Test
  public void testAffectedEntryForRenameChange() {
    root.doCreateFile(42, p("name"), null);

    Change c = new RenameChange(p("name"), "new name");
    c.applyTo(root);

    assertElements(new IdPath[]{idp(42)}, c.getAffectedEntryIdPaths());
  }

  @Test
  public void testCollectingDifferencesForRenameChange() {
    root.doCreateDirectory(1, p("dir"));
    root.doCreateFile(2, p("dir/file"), null);

    Change c = new RenameChange(p("dir/file"), "new name");
    c.applyTo(root);

    List<Difference> d1 =
        c.getDifferences(root, root.getEntry(p("dir/new name")));
    assertEquals(1, d1.size());
    assertTrue(d1.get(0).isModified());

    List<Difference> d2 = c.getDifferences(root, root.getEntry(p("dir")));
    assertEquals(1, d2.size());
    assertTrue(d2.get(0).isModified());
  }

  @Test
  public void testAffectedEntryForMoveChange() {
    root.doCreateDirectory(1, p("dir1"));
    root.doCreateDirectory(2, p("dir2"));
    root.doCreateFile(13, p("dir1/file"), null);

    Change c = new MoveChange(p("dir1/file"), p("dir2"));
    c.applyTo(root);

    assertElements(new IdPath[]{idp(1, 13), idp(2, 13)},
                   c.getAffectedEntryIdPaths());
  }

  @Test
  public void testCollectingDifferencesForMoveChange() {
    root.doCreateDirectory(1, p("dir1"));
    root.doCreateDirectory(2, p("dir2"));
    root.doCreateFile(3, p("dir1/file"), null);

    Change c = new MoveChange(p("dir1/file"), p("dir2"));
    c.applyTo(root);

    List<Difference> d1 = c.getDifferences(root, root.getEntry(p("dir1")));
    assertEquals(1, d1.size());
    assertTrue(d1.get(0).isDeleted());

    List<Difference> d2 = c.getDifferences(root, root.getEntry(p("dir2")));
    assertEquals(1, d2.size());
    assertTrue(d2.get(0).isCreated());

    List<Difference> d3 = c.getDifferences(root, root.getEntry(p("dir2/file")));
    assertEquals(1, d3.size());
    assertTrue(d3.get(0).isModified());
  }

  @Test
  public void testCollectingDifferencesForMoveChangeFromRoot() {
    root.doCreateFile(3, p("file"), null);
    root.doCreateDirectory(2, p("dir"));

    Change c = new MoveChange(p("file"), p("dir"));
    c.applyTo(root);

    List<Difference> d = c.getDifferences(root, root.getEntry(p("dir/file")));
    assertEquals(1, d.size());
    assertTrue(d.get(0).isModified());
  }

  @Test
  public void testCollectingDifferencesForMoveChangeForRootDir() {
    root.doCreateDirectory(1, p("root"));
    root.doCreateDirectory(2, p("root/dir1"));
    root.doCreateDirectory(3, p("root/dir2"));
    root.doCreateFile(4, p("root/dir1/file"), null);

    Change c = new MoveChange(p("root/dir1/file"), p("root/dir2"));
    c.applyTo(root);

    List<Difference> d = c.getDifferences(root, root.getEntry(p("root")));
    assertEquals(2, d.size());
    assertTrue(d.get(0).isDeleted());
    assertTrue(d.get(1).isCreated());
  }

  @Test
  public void testAffectedEntryForDeleteChange() {
    root.doCreateDirectory(99, p("dir"));
    root.doCreateFile(7, p("dir/file"), null);

    Change c = new DeleteChange(p("dir/file"));
    c.applyTo(root);

    assertElements(new IdPath[]{idp(99, 7)}, c.getAffectedEntryIdPaths());
  }

  @Test
  public void testCollectingDifferencesForDeleteChange() {
    root.doCreateDirectory(1, p("dir"));
    root.doCreateFile(2, p("dir/file"), null);

    Change c = new DeleteChange(p("dir/file"));
    c.applyTo(root);

    List<Difference> d = c.getDifferences(root, root.getEntry(p("dir")));
    assertEquals(1, d.size());
    assertTrue(d.get(0).isDeleted());
  }
}