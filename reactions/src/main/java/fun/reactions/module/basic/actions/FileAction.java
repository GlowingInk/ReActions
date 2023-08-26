package fun.reactions.module.basic.actions;

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * @author MaxDikiy
 * @since 05/07/2017
 */
public class FileAction implements Action {
    private static final String dir = new File("").getAbsolutePath();

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        String action = params.getString("action");
        String fileName = params.getString("fileName");
        String fileNameTo = params.getString("fileNameTo");
        if (action.isEmpty() || fileName.isEmpty()) return false;

        File file = new File(dir + File.separator + fileName);
        env.getVariables().set("fullpath", file.getAbsolutePath());

        if (action.equalsIgnoreCase("remove")) {
            int c = 0;
            if (file.isDirectory()) {
                String[] files = file.list();
                for (String subFile : files) {
                    if (new File(file, subFile).delete()) c++;
                }
            } else {
                if (file.delete()) c = 1;
            }
            env.getVariables().set("removecount", Integer.toString(c));
            return true;

        } else {
            if (fileNameTo.isEmpty()) return false;
            File fileTo = new File(dir + File.separator + fileNameTo);
            try {
                File fileToDir = new File(fileTo.getCanonicalPath());
                if (!fileToDir.exists()) fileToDir.mkdirs();
                if (file.isFile()) {
                    if (action.equalsIgnoreCase("copy")) {
                        Files.copy(file.toPath(), fileTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } else if (action.equalsIgnoreCase("move")) {
                        Files.move(file.toPath(), fileTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                    return true;
                }
            } catch (IOException e) {
                env.getVariables().set("filedebug", e.getLocalizedMessage());
            }

        }
        return false;
    }

    @Override
    public @NotNull String getName() {
        return "FILE";
    }
}
