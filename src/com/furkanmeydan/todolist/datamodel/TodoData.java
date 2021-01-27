package com.furkanmeydan.todolist.datamodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

public class TodoData {

    private static TodoData instance = new TodoData(); // instance adında bir todoData  nesnesi oluşturulur
    private static String fileNAme = "TodoListItems.txt"; // saklanacak verilerin file dosyası tanımlanır.

    private ObservableList<TodoItem> todoItems;  // todoitem tipinde değer alan bir observable list tanımlanır
    private DateTimeFormatter formatter;

    public static TodoData getInstance(){
        return instance;

    }

    private TodoData(){
        formatter=DateTimeFormatter.ofPattern("dd-MM-yyyy"); // tarihin formatı bbelirlenir.

    }

    public ObservableList<TodoItem> getTodoItems() {
        return todoItems;
    }



    public void addTodoItem(TodoItem item){  // yeni todoItem eklemek için parametre olarak Todoitem alan ve bunu listeye ekleyen metod tanımlanır
        todoItems.add(item);

    }

    public void loadTodoItems() throws IOException{  // bu metod bütün todoItemları yüklemek için

        todoItems = FXCollections.observableArrayList(); // todoItem'ın initiate'i yapılır

        Path path = Paths.get(fileNAme);  // saklanacak verilerin filePath'i verilir.

        BufferedReader br = Files.newBufferedReader(path); // Verilerin okunması için bir bufferedReader tanımlanır ve oluşturulan path içine atanır.

        String input; // bufferedReader ile okunacak bütün Tipler string olduğu için bir String oluşturulur.

        try{
            while((input=br.readLine())!=null){ // bufferedreader'ın okuyacağı nesne olduğu sürece...
                String[] itemPieces = input.split("\t"); // boşluğa göre STringi ayıran bir yapı yazılır
                String shortDescription = itemPieces[0]; // bu string array'inin birincisine shortDescription
                String details = itemPieces[1]; // ikincisine detay
                String dateString = itemPieces[2]; // üçüncüsüne de tarih ataması yapılır.

                LocalDate date = LocalDate.parse(dateString,formatter);

                TodoItem todoItem = new TodoItem(shortDescription,details,date); // Yeni bir todoItem nesnesi oluşturulur ve yukarıda alınan nesneler ile bu itema atama yapılr
                todoItems.add(todoItem); // Yukarıda oluşturulan bütün todoItem nesneleri observable arraylist içine atılır


            }
        }
        finally {
            if(br!=null){
                br.close();  // eğer bufferedReader'ın okuyacağı bir nesne yoksa BR kapatılırç
            }
        }
    }


    public void storeTodoItems() throws IOException{  // File içine nesne eklemek için de buradaki metot kullanılacak Yine aynı şekil path verilir ve BW bu pathe göre oluşturulr
        Path path = Paths.get(fileNAme);
        BufferedWriter bw = Files.newBufferedWriter(path);

        try{
            Iterator<TodoItem> iter = todoItems.iterator();  // TodoItem içinde dolaşacak bir iterator oluşturulur.
            while(iter.hasNext()){ // iterator devam ettiği sürece
                TodoItem item = iter.next();
                bw.write(String.format("%s\t%s\t%s", //Bir String formatı oluşturulur bu String format BufferedReader'ın istediğimiz şekilde okumasını sağlamamız için
                        // BR 'a uygun şekilde tanımlanması gerekiyor.
                        item.getShortDescription(),
                        item.getDetails(),
                        item.getDeadline().format(formatter)
                        ));

                bw.newLine();
            }
        }
        finally {
            if(bw!=null){
                bw.close();
            }
        }

    }

    public void deleteTodoItem(TodoItem item){
        todoItems.remove(item);
    }


}
