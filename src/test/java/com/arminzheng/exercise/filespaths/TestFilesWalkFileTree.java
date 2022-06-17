package com.arminzheng.exercise.filespaths;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 遍历目录文件 (使用 访问者模式)
 *
 * @since 2022-05-29
 */
@Slf4j
public class TestFilesWalkFileTree {

    /**
     * 遍历 检查 文件夹 和 文件数 使用 Files.walkFileTree( )
     *
     * <pre>
     *     输出为：前序遍历
     *     >>>>>>>>>>>>> 进入 目录 C:\aio1
     *     C:\aio1\1.jpg
     *     >>>>>>>>>>>>> 进入 目录 C:\aio1\aio2
     *     >>>>>>>>>>>>> 进入 目录 C:\aio1\aio2\aio3
     *     <<<<<<<<<<<<< 退出 目录 C:\aio1\aio2\aio3
     *     C:\aio1\aio2\hello.txt
     *     <<<<<<<<<<<<< 退出 目录 C:\aio1\aio2
     *     >>>>>>>>>>>>> 进入 目录 C:\aio1\aio21
     *     C:\aio1\aio21\a.txt
     *     <<<<<<<<<<<<< 退出 目录 C:\aio1\aio21
     *     C:\aio1\hello.txt
     *     <<<<<<<<<<<<< 退出 目录 C:\aio1
     *     文件夹：4 会把自身算进去，就多了一个
     *     文件数：4
     * </pre>
     */
    @Test
    public void traverse() throws IOException {
        // 这里不能使用 局部变量 int, 因为 内部类使用局部变量 必须都是 final 的，java1.8后可以不写final
        final AtomicInteger dirCount = new AtomicInteger();
        final AtomicInteger fileCount = new AtomicInteger();
        // SimpleFileVisitor uses the visitor pattern.
        Files.walkFileTree(
                Paths.get("C:\\aio1"),
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                            throws IOException {
                        System.out.println(">>> 进入 目录 " + dir);
                        dirCount.incrementAndGet();
                        return super.preVisitDirectory(dir, attrs);
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException {
                        // if (file.toString().endsWith(".jar")) // 加判断
                        System.out.println(file);
                        // Files.delete(file); // =================== 删除文件
                        fileCount.incrementAndGet();
                        return super.visitFile(file, attrs);
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                            throws IOException {
                        // Files.delete(dir); // =================== 删除文件夹
                        System.out.println("<<< 退出 目录 " + dir);
                        return super.postVisitDirectory(dir, exc);
                    }
                });
        System.out.println("文件夹：" + dirCount);
        System.out.println("文件数：" + fileCount);
    }

    /** 遍历 检查 jar包 */
    @Test
    public void traverseJarPackages() throws IOException {
        // 这里不能使用 局部变量 int, 因为 内部类使用局部变量 必须都是 final 的，java1.8后可以不写final
        final AtomicInteger jarCount = new AtomicInteger();
        Files.walkFileTree(
                Paths.get("C:\\JAVA\\code\\springboot_code\\032-springboot-servlet-2"),
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException {
                        if (file.toString().endsWith(".jar")) {
                            System.out.println(file);
                            jarCount.incrementAndGet();
                        }
                        return super.visitFile(file, attrs);
                    }
                });
        System.out.println("jar count: " + jarCount);
    }

    /**
     * 遍历「删除」文件
     *
     * <p>删除文件夹必须在「访问后方法」里面处理 postVisitDirectory
     */
    @Test
    public void traverseDelete() throws IOException {
        // 这里不能使用 局部变量 int, 因为 内部类使用局部变量 必须都是 final 的，java1.8后可以不写final
        final AtomicInteger fileCount = new AtomicInteger();
        Files.walkFileTree(
                Paths.get("C:\\aio1 - 副本 - 副本"),
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException {
                        System.out.println(file);
                        fileCount.incrementAndGet();
                        Files.delete(file); // =================== 删除文件
                        return super.visitFile(file, attrs);
                    }
                    // 退出目录 处理
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                            throws IOException {
                        Files.delete(dir); // =================== 删除文件夹
                        return super.postVisitDirectory(dir, exc);
                    }
                });
        System.out.println("文件数：" + fileCount);
    }

    @Test
    public void incrementAndGet() {
        int a = 88;
        a++;
        System.out.println(a);

        AtomicInteger number = new AtomicInteger();
        // There is no way to change local variables in anonymous classes, they are all final
        number.incrementAndGet();
        // So we use AtomicInteger
        number.incrementAndGet();
        System.out.println(number);
        log.info("number = {}", number.get());
    }
}
