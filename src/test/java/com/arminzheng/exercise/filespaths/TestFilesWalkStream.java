package com.arminzheng.exercise.filespaths;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * 复制「整个」目录
 *
 * @since 2022-05-29
 */
public class TestFilesWalkStream {

    @Test
    public void testCopyDirAll() throws IOException {
        String source = "/Users/armin/Desktop/testDir";
        String target = "/Users/armin/Desktop/testDirTo";
        // 同样也是遍历
        try (Stream<Path> traverseStream = Files.walk(Paths.get(source))) {
            traverseStream.forEach(
                    path -> {
                        String targetName = path.toString().replace(source, target);
                        try {
                            Path currentPath = Paths.get(targetName);
                            if (Files.isDirectory(path)) { // 如果是 目录 创建
                                Files.createDirectories(currentPath);

                            } else if (Files.isRegularFile(path)) { // 如果是 文件 复制
                                Files.copy(path, currentPath);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }
}
