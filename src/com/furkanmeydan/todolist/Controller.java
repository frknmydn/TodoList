package com.furkanmeydan.todolist;

import com.furkanmeydan.todolist.datamodel.TodoData;
import com.furkanmeydan.todolist.datamodel.TodoItem;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {
    @FXML
    private ListView<TodoItem> todoListView;   //fxml içerisindeki tiplerin id'si ile aynı id'de tanımlama yapılır.
    @FXML
    private TextArea itemDetailTextArea;
    @FXML
    private Label deadlineLabel;
    @FXML
    private BorderPane mainBorderPane;

    private List<TodoItem> todoItems;

    @FXML
    private ContextMenu listContextMenu;

    @FXML
    private ToggleButton filteredToggleButton;

    private FilteredList<TodoItem> filteredList;  //Itemların filtrelenmesini (Toogle button ile o günün itemlarını listelemek) için bir TodoItem tipinde filteredList tanımlanır
    private Predicate<TodoItem> wantAllItems;  // bütün itemların listelenmesini istediğimizde çağıracağımız Predicate
    private Predicate<TodoItem> todaysItem;  // o günün itemlarının listelenmesini istediğimizde çağıracağımız Predicate

    public void initialize(){

        wantAllItems=new Predicate<TodoItem>() {  // bütün itemların listelenmesi için test metodunu true döndürüyoruz.
            @Override
            public boolean test(TodoItem todoItem) {
                return true;
            }
        };

        todaysItem= new Predicate<TodoItem>() {  // ogünün itemlarının listelenmesi için return içinde bir koşul belirtiyoruz.
            @Override
            public boolean test(TodoItem todoItem) {
                return todoItem.getDeadline().equals(LocalDate.now());
            }
        };

        listContextMenu=new ContextMenu();  // listView içindeki bir item'a sağ tıkladığımızda açılacak pencereyi tanımlıyoruz.
        MenuItem deleteMenuItem = new MenuItem("Delete");   //  yukarıda tanımladıktan sonra delete Yazan bir menu item tanımlıyoruz.
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) { // hnadle metodu ile delete'e tıklandığında list içinde seçilen item'ın silinmesini sağlıyoruz.
                TodoItem item  = todoListView.getSelectionModel().getSelectedItem();
                deleteItem(item);
            }
        });

        listContextMenu.getItems().addAll(deleteMenuItem); // contextMenu içinde tanımlanan deleteMEnuItem tanımlanır.


        todoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TodoItem>() { // liste'de tıklanan item'ın propertyLerinin gelmesi için:
            @Override
            public void changed(ObservableValue<? extends TodoItem> observable, TodoItem oldValue, TodoItem newValue) {
                if(newValue!=null){
                    TodoItem item = todoListView.getSelectionModel().getSelectedItem(); // bir Nesne tanımlanır ve tanımlanan nesne'nin içine tıklanan item getirtilir.
                    itemDetailTextArea.setText(item.getDetails());
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("d MMMM  yyyy");
                    deadlineLabel.setText(df.format(item.getDeadline()));

                    //https://docs.oracle.com/javafx/2/api/javafx/scene/control/SelectionModel.html#:~:text=selectedItemProperty(),item%20in%20the%20selection%20model.
                }
            }
        });

        filteredList=new FilteredList<>(TodoData.getInstance().getTodoItems(), wantAllItems); // listenin default görünümünün bütün itemlar dahil hali olması için.

        SortedList<TodoItem> sortedList = new SortedList<TodoItem>(filteredList, new Comparator<TodoItem>() {// listeyi tarihe göre sıralamak için
            @Override
            public int compare(TodoItem o1, TodoItem o2) {
                return o1.getDeadline().compareTo(o2.getDeadline());

            }
        });


        todoListView.setItems(sortedList); // tarihe göre sıralanmış itemları listView içinde gösteriyoruz.
        todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); // listviewa bir seçim yapılması için
        todoListView.getSelectionModel().selectFirst(); //1. seçili olarak başlar

        todoListView.setCellFactory(new Callback<ListView<TodoItem>, ListCell<TodoItem>>() { // itemların tarihine göre yazı colorının değişmesini istiyoruz bunun için cellfactory kullanılıyor
            @Override
            public ListCell<TodoItem> call(ListView<TodoItem> param) {
                ListCell<TodoItem> cell = new ListCell<TodoItem>(){
                    @Override
                    protected void updateItem(TodoItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty){
                            setText(null);
                        }
                        else{
                            setText(item.getShortDescription());
                            if(item.getDeadline().isBefore((LocalDate.now().plusDays(1)))){ // eğer tarih geçmiş ise listView'da textin rengi kırmızı
                                setTextFill(Color.RED);
                            }
                            else if(item.getDeadline().equals(LocalDate.now().plusDays(1))){
                                setTextFill(Color.GREEN); // eğer tarih gelecekte ise rengi yeşil yapıyoruz.
                            }
                        }

                    }
                };

                cell.emptyProperty().addListener(
                        (obs,wasEmpty,isNowEmpty) ->{
                            if(isNowEmpty){
                                cell.setContextMenu(null);
                            }
                            else {
                                cell.setContextMenu(listContextMenu);
                            }
                        }

                );

                return cell;
            }
        });

    }
    @FXML
    public void showNewItemDialog(){ // bu bizim yukarıda tanımlanmımş ve yeni todoItem eklemek için yapılmış butonun onAction'ı ile aynı isme sahip olması gereken metot

        Dialog<ButtonType> dialog = new Dialog<>(); // yeni bir dialog nesnesi oluşturuyoruz.
        dialog.setTitle("New todoItem");
        dialog.setHeaderText("Use this dialog to create new Item");
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        FXMLLoader fxmlLoader = new FXMLLoader(); // gözükmesi için fxmlLoadre kullanıyoruz.
        fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml")); // ve gözükecek sayfanın fxml file'ını tanımlıyoruz.

        try {

            dialog.getDialogPane().setContent(fxmlLoader.load());
        }
        catch (IOException e){
            System.out.println("we couldnt load the dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK); // iki adet buton tanımlıyoruz. biri Ok diğeri Cancel
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK){ // eğer basılan buton ok ise

            DialogController controller = fxmlLoader.getController();
            TodoItem newItem = controller.processResult(); // processresult DialogController içinde tanımlanmış ve yeni item ataması yapan bir metot
            todoListView.getSelectionModel().select(newItem);
        }


    }

    @FXML
    public void handleKeyPressed(KeyEvent keyEvent){ // handleKeyPressed metodu listView'in onAction'dakiyle aynı olmalı
        TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if(selectedItem!=null){
            if(keyEvent.getCode().equals(KeyCode.DELETE)){ // eğer açılan pencerede tıklanan keyEvent butonu(!) delete ise:
                deleteItem(selectedItem); // seçilen itemı sil
            }
        }

    }


    @FXML
    public void handleClickListView(){
        TodoItem item = todoListView.getSelectionModel().getSelectedItem();  //click atıldığında gelecek item.
        itemDetailTextArea.setText(item.getDetails());
        deadlineLabel.setText(item.getDeadline().toString());
        todoListView.getSelectionModel().selectFirst();


    }

    public void deleteItem(TodoItem item){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); // kullanıcının son kez onayını almak için alert oluşturulur
        alert.setTitle("Deletee Todo Item");
        alert.setHeaderText("Delete item" + item.getShortDescription());
        alert.setContentText("Are You sure?");

        Optional<ButtonType> result = alert.showAndWait();

        if(result.isPresent() && (result.get()==ButtonType.OK)){
            TodoData.getInstance().deleteTodoItem(item); // eğer onayı verdiyse seçilen item silinir

        }


    }
    @FXML
    public void handleFilteredButton(){ // bu metodun adı da toogleItem'ın onActionu ile aynı olmak zorunda
        TodoItem selectedITem = todoListView.getSelectionModel().getSelectedItem(); // selectedItem adında bir todoItem nesnesi tanımlanır
        if(filteredToggleButton.isSelected()) { // eğer togglebuttona basıldıysa

            filteredList.setPredicate(todaysItem); // bugünün itemlarını getir. Bu metodun çalışması için filteredList'in tanımlamaları yulkarıda yapıldı
            if(filteredList.isEmpty()){ // eğer filteredList'te item yoksa alanlar temizlenir
                itemDetailTextArea.clear();
                deadlineLabel.setText("");

            }
            else if(filteredList.contains(selectedITem)){ // eğer filteredList içinde selectedItem varsa
                todoListView.getSelectionModel().select(selectedITem); // selecTedItem seçilir
            }
            else{
                todoListView.getSelectionModel().selectFirst(); // eğer birden fazla varsa birinci seçili halde getirlir.
            }
        }
        else{ // eğer togglebutton seçili değilse

            filteredList.setPredicate(wantAllItems);  // bütün itemler listelenir.
            todoListView.getSelectionModel().select(selectedITem); // toogleButton seçili iken seçilmiş item bütün itemlar listelendiğinde de seçili halde gelir
        }

    }
    @FXML
    public void handleExit(){// fxml'de menuItem'ın exit isimli onAction tanımlaması ile aynı bir metot tanımlanır
        Platform.exit(); // butona basıldığında programı kapatır.
    }

}
