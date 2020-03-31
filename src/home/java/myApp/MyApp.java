package myApp;

import com.jfoenix.controls.JFXDecorator;
import controllers.HomeController;
import io.datafx.controller.flow.Flow;
import io.datafx.controller.flow.container.DefaultFlowContainer;
import io.datafx.controller.flow.context.FXMLViewFlowContext;
import io.datafx.controller.flow.context.ViewFlowContext;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MyApp extends Application{

    @FXMLViewFlowContext
    private ViewFlowContext flowContext;

    @Override
    public void start(Stage primaryStage) throws Exception {

        primaryStage.setTitle("OnlyViewer");

        /* Copy from JFoenix Demo */
        Flow flow = new Flow(HomeController.class);
        DefaultFlowContainer container = new DefaultFlowContainer();
        flowContext = new ViewFlowContext();
        flowContext.register("Stage", primaryStage);
        flow.createHandler(flowContext).start(container);

        //自定义JFX的窗口边框样式
        JFXDecorator decorator = new JFXDecorator(primaryStage, container.getView());
        decorator.setCustomMaximize(true);

        //设置标题栏左侧小图标
        //FIXME 无法使用相对路径加载中resources/icons/下的app.png 考虑换用getResource()
        decorator.setGraphic(new ImageView("./app.png"));
//        decorator.setGraphic(new ImageView("../resources/icons/app.png"));

        //下面根据屏幕大小自适应设置长宽
        double width = 800;
        double height = 600;
        try {
            Rectangle2D bounds = Screen.getScreens().get(0).getBounds();
            width = bounds.getWidth() / 1.35;
            height = bounds.getHeight() / 1.35;
        } catch (Exception e){
            e.printStackTrace();
        }
        Scene scene = new Scene(decorator, width, height);

        //加载css样式文件
        final ObservableList<String> stylesheets = scene.getStylesheets();
        stylesheets.addAll(MyApp.class.getResource("/css/home.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
