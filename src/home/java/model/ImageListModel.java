package home.java.model;

import lombok.Data;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

/**
 * @ProjName: OnlyViewer
 * @ClassName: ImageListModel
 * @Author: Kevin
 * @Time:2020/3/18 11:11
 * @Describe: 文件夹内的图片列表2.0
 * 1.文件筛选 2.计算图片数 3.创建图片列表 4.计算文件夹所有图片的大小
 **/

@Data
public class ImageListModel {

//    private static String type;
//
//    private static int width;
//    private static int height;

    // 判断文件是否为图片 支持jpg/jpeg/png/gif/bmp,暂不支持psd
    public static boolean isSupportedImg(String fileName){
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                fileName.endsWith(".png") || fileName.endsWith(".gif") ||
                fileName.endsWith(".bmp");
    }

    // 初始化图片列表
    public static ArrayList<ImageModel> initImgList(String path) throws IOException{
        ArrayList<ImageModel> imgList = new ArrayList<>(); // 默认根据name进行排序
        Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs){
                String fileName = file.getFileName().toString().toLowerCase();
                if (isSupportedImg(fileName)){
                    imgList.add(new ImageModel(file.toString())); // 获取绝对路径
                }
                return FileVisitResult.CONTINUE;
            }

            // 只访问当前文件夹 不进行递归访问
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs){
                    if (dir.toString().equals(path)){
                        return FileVisitResult.CONTINUE;
                    }else
                        return FileVisitResult.SKIP_SUBTREE;
            }

            // 处理访问系统文件的异常
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException{
                System.out.println("不访问系统文件夹");
                return FileVisitResult.SKIP_SUBTREE;
            }
        });
        return imgList;
    }

    // 返回文件夹内图片张数
    public static int getListImgNum(ArrayList<ImageModel> im){
        return im.size();
    }

    // 返回文件夹内的 图片 大小
    public static String getListImgSize(ArrayList<ImageModel> im){
        long totalSize = 0;
        for (ImageModel i:im){
            totalSize += i.getFileLength();
        }
        return GenUtilModel.getFormatSize(totalSize);
    }

    // 刷新文件夹 返回新列表
    public static ArrayList<ImageModel> refreshList(String path){
        ArrayList<ImageModel> list = new ArrayList<>();
        try{
            list = initImgList(path);
        }catch (IOException e){
            System.out.println("IOException");
        }
        return list;
    }

    @Test
    public void Test1() throws IOException {
        String filePath = "F:\\Ding\\LIFE\\phone";
        long timef;
        long timel;
        timef = System.currentTimeMillis();
        ArrayList<ImageModel> list = initImgList(filePath);
        timel = System.currentTimeMillis();
        System.out.printf("初始化耗时：%d ms\n", (timel-timef));
        if (list.size()==0){
            System.out.println("There is no image!");
        }else {
//            for (ImageModel i : list){
//                System.out.println("imgName:"+i.getImageName()+
//                        "\t\timgLastModified:"+i.getFormatTime()+
//                        "\t\timgSize:"+i.getFormatSize());
//            }
            System.out.println("totalImgNum:" + getListImgNum(list));
            System.out.println("totalImgSize:" + getListImgSize(list));
//            timef = System.currentTimeMillis();
//            refreshList(filePath);
//            System.out.println("刷新成功！");
//            timel = System.currentTimeMillis();
//            System.out.printf("耗时：%d ms\n", (timel-timef));
        }

        System.out.println("测试成功!");
    }
}