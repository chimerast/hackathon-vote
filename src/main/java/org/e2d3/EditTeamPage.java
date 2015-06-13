package org.e2d3;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.lang.Bytes;

public class EditTeamPage extends WebPage {

    public static final String ROOT_DIR = "/data/upload";

    private User user;
    private Team team;

    private List<FileUpload> uploaded;

    public EditTeamPage() {
        this.user = DAO.getUser(UserData.get().getUserId());

        this.team = DAO.getTeam(user.teamId);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        Form<?> editForm = new Form<Object>("editForm") {
            @Override
            protected void onSubmit() {
                DAO.save(team);

                Path destDir = Paths.get(ROOT_DIR, team.id);

                try {
                    if (Files.exists(destDir)) {
                        deleteDirectory(destDir);
                    }

                    createDirectory(destDir);

                    if (uploaded != null) {
                        FileUpload f = uploaded.get(0);
                        File file = f.writeToTempFile();
                        try {
                            inflate(file, destDir);
                        } finally {
                            if (!file.delete()) {
                                file.deleteOnExit();
                            }
                        }
                    }

                    Path macAttrs = destDir.resolve("__MACOSX");
                    if (Files.exists(macAttrs)) {
                        deleteDirectory(macAttrs);
                    }

                    resolveDirectory(destDir);

                    setResponsePage(HomePage.class);
                } catch (IOException e) {
                    error(e.getMessage());
                }
            }
        };

        editForm.setDefaultModel(new CompoundPropertyModel<Team>(team));
        editForm.setMultiPart(true);
        editForm.setMaxSize(Bytes.megabytes(100));

        add(editForm);

        editForm.add(new Label("id"));
        editForm.add(new TextField<String>("title"));
        editForm.add(new TextField<String>("e2d3"));
        editForm.add(new TextArea<String>("comment"));
        editForm.add(new FileUploadField("file", new PropertyModel<List<FileUpload>>(this, "uploaded")));
        editForm.add(new BookmarkablePageLink<Void>("cancel", HomePage.class));

        add(new FeedbackPanel("feedback"));
    }

    private void deleteDirectory(Path destDir) throws IOException {
        Files.walkFileTree(destDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException {
                Files.delete(path);
                return checkNotExist(path);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path path, IOException exception) throws IOException {
                if (exception == null) {
                    Files.delete(path);
                    return checkNotExist(path);
                } else {
                    throw exception;
                }
            }

            private FileVisitResult checkNotExist(final Path path) throws IOException {
                if (!Files.exists(path)) {
                    return FileVisitResult.CONTINUE;
                } else {
                    throw new IOException();
                }
            }
        });
    }

    private void createDirectory(Path destDir) throws IOException {
        if (Files.notExists(destDir)) {
            Files.createDirectory(destDir);
        }
    }

    private void resolveDirectory(Path destDir) throws IOException {
        List<Path> files = Files.list(destDir).collect(Collectors.toList());
        if (files.size() == 1 && Files.isDirectory(files.get(0))) {
            Files.list(files.get(0)).forEach((p) -> {
                try {
                    Files.move(p, destDir.resolve(p.getFileName()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void inflate(File zipFile, Path destDir) throws IOException {
        URI zipUri = URI.create("jar:file:" + zipFile.getAbsolutePath());

        Map<String, String> env = new HashMap<>();
        env.put("create", "false");
        env.put("encoding", "Windows-31J");

        try (FileSystem zipfs = FileSystems.newFileSystem(zipUri, env)) {
            final Path root = zipfs.getPath("/");
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path destFile = Paths.get(destDir.toString() + file.toString());
                    Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path dirToCreate = Paths.get(destDir.toString() + dir.toString());
                    if (Files.notExists(dirToCreate)) {
                        Files.createDirectory(dirToCreate);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

}
