import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;

public class Controller {

    @FXML private BorderPane borderPane;
    @FXML private VBox leftPane, rightPane, centerVBox;
    @FXML private MediaView mediaView;
    @FXML private ImageView audioImage;
    @FXML private ProgressBar progressBar;
    @FXML private Button btnPlay;
    @FXML private Label lblDuration;
    @FXML private Label lblTitulo;
    @FXML private TableView<File> tablaBiblioteca;
    @FXML private TableColumn<File, String> colNombre;
    @FXML private TableColumn<File, String> colFormato;
    //@FXML private TableColumn<File, String> colDuracion;

    private MediaPlayer mediaPlayer;
    private boolean isLeftVisible = true;
    private boolean isRightVisible = true;
    private boolean isClaro = true;
    private static final String RESOURCE_DIRECTORY = "src/recursos";

    private boolean isSizeSmall = false; 
    @FXML
    private void cambiarTamano() {
        if (mediaPlayer != null) {
            if (isSizeSmall) {
                
                mediaView.setFitWidth(mediaView.getFitWidth() * 2);
                mediaView.setFitHeight(mediaView.getFitHeight() * 2);
                audioImage.setFitWidth(audioImage.getFitWidth() * 2);
                audioImage.setFitHeight(audioImage.getFitHeight() * 2);
            } else {
               
                mediaView.setFitWidth(mediaView.getFitWidth() / 2);
                mediaView.setFitHeight(mediaView.getFitHeight() / 2);
                audioImage.setFitWidth(audioImage.getFitWidth() / 2);
                audioImage.setFitHeight(audioImage.getFitHeight() / 2);
            }
            isSizeSmall = !isSizeSmall;
        }
    }
    

    public void initialize() { 
    
    borderPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
        if (newScene != null) {
            newScene.heightProperty().addListener((obsHeight, oldHeight, newHeight) -> ajustarTamanoMediaView());
            newScene.widthProperty().addListener((obsWidth, oldWidth, newWidth) -> ajustarTamanoMediaView());
        }
    });
        
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(getFileName(cellData.getValue())));

        colFormato.setCellValueFactory(cellData -> new SimpleStringProperty(getFileExtension(cellData.getValue())));
       // colDuracion.setCellValueFactory(cellData -> new SimpleStringProperty(getFileDuration(cellData.getValue())));
    
        cargarBiblioteca();
    
        tablaBiblioteca.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { 
                selectMedia();
            }
        });
        ajustarTamanoMediaView();
    }

    private void ajustarTamanoMediaView() {
        double alturaDisponible = borderPane.getHeight() - 90; 
        double anchuraDisponible = borderPane.getWidth() - ((isLeftVisible ? 250 : 0) + (isRightVisible ? 250 : 0));
    
        mediaView.setPreserveRatio(true);
        audioImage.setPreserveRatio(true);
    
        mediaView.setFitHeight(alturaDisponible);
        audioImage.setFitHeight(alturaDisponible);
    
       
        double nuevoAnchoMedia = mediaView.getBoundsInParent().getWidth();
        double nuevoAnchoImagen = audioImage.getBoundsInParent().getWidth();
    
        
        if (nuevoAnchoMedia > anchuraDisponible) {
            mediaView.setFitWidth(anchuraDisponible);
        }
        if (nuevoAnchoImagen > anchuraDisponible) {
            audioImage.setFitWidth(anchuraDisponible);
        }
    }
    
    

    private void cargarBiblioteca() {
        File folder = new File("src/recursos");
    if (folder.exists() && folder.isDirectory()) {
        File[] files = folder.listFiles((dir, name) ->     name.endsWith(".mp4") || name.endsWith(".mp3") || name.endsWith(".wav") //anadir más tipos de archivos?
    );
        if (files != null) {
            tablaBiblioteca.getItems().addAll(files);
        }
    }
    }
    private String getFileName(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf(".");
        return (lastIndex == -1) ? name : name.substring(0, lastIndex);
    }
    
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf(".");
        return (lastIndex == -1) ? "Desconocido" : name.substring(lastIndex + 1);
    }
    
    
    /*private String getFileDuration(File file) {
        Media media = new Media(file.toURI().toString());
        Duration duration = media.getDuration();
        double durationInSeconds = duration.toSeconds();
      return String.valueOf(durationInSeconds);
    }*/

    //Resolver NaN




    @FXML
private void selectMedia() {
    File selectedFile = tablaBiblioteca.getSelectionModel().getSelectedItem();
    if (selectedFile != null) {
        try {
           
            if (mediaPlayer != null) {
                mediaPlayer.dispose();
            }

            String url = selectedFile.toURI().toString();
            Media media = new Media(url);
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);

            lblTitulo.setText(getFileName(selectedFile));

            if (selectedFile.getName().endsWith(".wav")||selectedFile.getName().endsWith(".mp3")) {
                mediaView.setVisible(false);
                audioImage.setVisible(true);
                audioImage.setImage(new Image("file:src/recursos/desesperacion.jpg"));
            } else {
                mediaView.setVisible(true);
                audioImage.setVisible(false);
            }

            mediaPlayer.setOnReady(() -> {
                Duration totalDuration = media.getDuration();
                lblDuration.setText("Duración: 00 / " + (int) totalDuration.toSeconds());
                mediaPlayer.play(); 
            });

            mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                if (mediaPlayer.getTotalDuration() != Duration.UNKNOWN) {
                    double progress = newValue.toSeconds() / mediaPlayer.getTotalDuration().toSeconds();
                    progressBar.setProgress(progress);
                    lblDuration.setText("Duración: " + (int) newValue.toSeconds() + " / " + (int) mediaPlayer.getTotalDuration().toSeconds());
                }
            });

            mostrarDialogoNoModal("Archivo abierto", "Reproduciendo: " + selectedFile.getName()+"\n"+"Si tarda en cargar volver a pulsar");

        } catch (Exception e) {
            mostrarDialogoModal("Error", "No se pudo cargar el archivo.");
        }
    }
}


    @FXML
    private void play() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    @FXML
    private void pausa() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @FXML
    private void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    @FXML
    private void velocidadLenta() {
        if (mediaPlayer != null) {
            mediaPlayer.setRate(0.5);
        }
    }

    @FXML
    private void velocidadNormal() {
        if (mediaPlayer != null) {
            mediaPlayer.setRate(1.0);
        }
    }

    @FXML
    private void velocidadRapida() {
        if (mediaPlayer != null) {
            mediaPlayer.setRate(2.0);
        }
    }

    @FXML
private void abrirArchivo() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos Multimedia", "*.mp4", "*.mp3","*.jpg"));
    File file = fileChooser.showOpenDialog(new Stage());

    if (file != null) {
        tablaBiblioteca.getItems().add(file); 
    } ajustarTamanoMediaView();
}


    @FXML
    private void recargarBiblioteca() {
        tablaBiblioteca.getItems().clear();
        cargarBiblioteca();
    }

    @FXML
    private void cerrarApp() {
        System.exit(0);
    }

    @FXML
    private void activarModoOscuro() {
        borderPane.getStylesheets().clear();
        if(isClaro){ 
            borderPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("oscuro.css")).toExternalForm());
        }else{
                borderPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
        }
       
        isClaro = !isClaro;
    }

    @FXML
    private void mostrarInformacion() {
       /* File selectedFile = tablaBiblioteca.getSelectionModel().getSelectedItem();
        String nombre = getFileName(selectedFile); 
        String tipo = getFileExtension(selectedFile);
       //String duracion = getFileDuration(selectedFile);

        String mensaje = "Nombre: " + nombre + "\nTipo: " + tipo/*+"\nDuración: " + duracion*/;
        mostrarDialogoModal("Acerca de",/*mensaje */ "Reproductor multimedia \n Autor: Raposo");
    }

    @FXML
    private void toggleLeftPane() {
        if (isLeftVisible) {
            leftPane.setPrefWidth(0);
        } else {
            leftPane.setPrefWidth(200);
        }
        ajustarTamanoMediaView();
        isLeftVisible = !isLeftVisible;
    }

    @FXML
    private void toggleRightPane() {
        if (isRightVisible) {
            rightPane.setPrefWidth(0);
        } else {
            rightPane.setPrefWidth(150);
        }
        ajustarTamanoMediaView();
        isRightVisible = !isRightVisible;
    }

    private void mostrarDialogoModal(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarDialogoNoModal(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.initOwner(borderPane.getScene().getWindow());
        alert.show();
    }
}
