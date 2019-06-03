
package aplicacion;




import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;



public class Sistema implements Initializable {
    
    @FXML
    private Label label;
    
    @FXML
    private Button correr;
    
    @FXML
    private Button b1;
    
    @FXML
    private Button b5;
    
    @FXML
    private Button b2;
    
    @FXML
    private Button b3;
    
    @FXML
    private TextArea area;
    
    @FXML
    private Button b4;
    
    @FXML
    private Button b6;
    
    @FXML
    private Button b7;
    
    @FXML
    private Canvas canvas;
    
    @FXML
    private AnchorPane ap;
    
    private ArrayList<Figura> figuras = new ArrayList<>();
    
    private GraphicsContext gc;
    
    private Figura aux = null;
    
    private Figura f;
    public double powerUp=0;
    boolean b = false;
    
    ArrayList<Variable> variables = new ArrayList<>();
    
    @FXML
    public void mover()  {
        
        canvas.setOnMousePressed(e1->{
            aux = buscarConexion(e1.getX(), e1.getY());
            canvas.setOnMouseDragged(e2->{ 
                if(aux!=null && moviendo){
                    System.out.println("Manteniendo");
                    redimensionCanvas(e2.getX(), e2.getY());
                   
                    aux.dibujar(gc, e2.getX(), e2.getY());


                    actualizar();
                }else if(!moviendo){
                    moviendo=true;
                }
            });

            canvas.setOnMouseReleased(e3->{

                System.out.println("Soltado");
                moviendo = false;
                
                aux=null;



            });
        });
               
        
    }
    
    @FXML
    private void dibujarProceso(ActionEvent event) throws IOException {
        
        
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Crear proceso");
        dialog.setContentText("Introduzca el texto:");

        
        Optional<String> result = dialog.showAndWait();
        System.out.println(""+evaluarAritmetica(result.get().split("=")[1],variables));
        if (result.get().matches("[A-Za-z1-9]+=.+")){
           
   
            if(contar(result.get())>12){
                powerUp = 80;
                boolean a = true;
            }
            
            b=true;
            canvas.setOnMouseClicked((MouseEvent event2) -> {
            double p1 = event2.getX();
            double p2 = event2.getY();
            
            redimensionCanvas(p1,p2);
            
            
            if(b){
                
                
                
                double x = event2.getX();
                double y = event2.getY();
                double x1 = x - 50;
                double y1 = y - 25;
                double x2 = x + 50+powerUp;
                double y2 = y - 25;
                double x3 = x + 50+powerUp;
                double y3 = y + 25;
                double x4 = x - 50;
                double y4 = y +25;
                /***
                 * caso en que el punto se crea muy arriba o si el punto se crea
                 * muy a la izquierda
                 */

                if(x1<0){
                    x=50;
                    x2=x2-x1;
                    x3=x3-x1;
                    x1=0;
                    x4=0;
                }
                
                if(y1<0){
                    y=25;
                    y3=y3-y1;
                    y4=y4-y1;
                    y1=0;
                    y2=0;
                }
               
                   
                Proceso proceso = new Proceso( TipoF.PROCESO);
                proceso.setVerticeCentro(new Vertice(x,y));
                proceso.getVertices().add(new Vertice(x1, y1));
                proceso.getVertices().add(new Vertice(x2, y2));
                proceso.getVertices().add(new Vertice(x3, y3));
                proceso.getVertices().add(new Vertice(x4, y4));
                proceso.calcularConexiones();
                proceso.texto = result.get();
                proceso.setEstado(true);
                figuras.add(proceso);
                proceso.dibujar(gc);
                    
                
                actualizar();
                b=false;
                
                powerUp=0;
                canvas.setOnMouseClicked(null);
            }  
        });
            
        } else {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Error");
            alert.setHeaderText("cuidado");
            alert.setContentText("El formato de texto ingresado es incorrecto");

            alert.showAndWait();
        }

        
        

        
    }
    
    public boolean comprobarPadres(Flujo flujo){
        for (Figura figura : figuras) {
            if(figura instanceof Flujo){
                if(flujo.padre.getVerticeCentro().distancia(((Flujo) figura).padre.getVerticeCentro())==0
                        || flujo.hijo.getVerticeCentro().distancia(((Flujo) figura).hijo.getVerticeCentro())==0){
                    return false;
                }
            }
            
        }
        return true;
    }
    
        
    private boolean buscarInicio(){ 
        for (Figura figura : figuras) {           
            if(figura.getTipo().equals(TipoF.INICIO)){
                return true;
            }
        }    
        return false;
    }
    
    private boolean buscarFin(){ 
        for (Figura figura : figuras) {           
            if(figura.getTipo().equals(TipoF.FIN)){
                return true;
            }
        }    
        return false;
    }
        
    
    
    
    
    @FXML
    private void correr(ActionEvent event) throws InterruptedException {
        
        Flujo flujo = null;
        Figura fig = null;
        ArrayList<Figura> corredors = new ArrayList<>(); 
        b=true;
        if(b){
            if (!figuras.isEmpty()){
                if(buscarInicio() && buscarFin()){
                    //buscar inicio
                    for (Figura figura : figuras) {
                        if(figura.getTipo() == TipoF.INICIO){
                            fig = figura;
                        }
                    }
                    // buscarFlujo que tenga el inicio
                    for (Figura figura : figuras) {
                        if(figura instanceof Flujo){
                            if(((Flujo) figura).padre.equals(fig)){
                                flujo = (Flujo) figura;
                            }
                        }
                    }
                    if(!flujo.equals(null)){
                        System.out.println(fig.texto);
                        corredors.add(fig);


                        //actualizar();

                        while(fig != null && fig.getTipo() != TipoF.FIN ){
                            fig = figuras.get(flujo.indexHijo);
                            for (Figura figura : figuras) {
                                if(figura instanceof Flujo){
                                    if(((Flujo) figura).padre.equals(fig)){
                                        flujo = (Flujo) figura;
                                    }
                                }
                            }
                            if(fig.tipo != TipoF.FIN){
                               System.out.println(fig.texto);
                               corredors.add(fig);
                            }
                        }
                        if(fig == null){
                            System.out.println("Mala construccion");
                        }else if(fig.getTipo() == TipoF.FIN){
                            System.out.println(fig.texto);
                            corredors.add(fig);
                        }

                        gc.setFill(Color.RED);

                        Thread hilo = new Thread(new Runnable() {
                            @Override
                            
                            public void run() {


                                    for (Figura corredor : corredors) {
                                        if (corredor.tipo!=TipoF.FIN && corredor.tipo!=TipoF.INICIO){
                                            agregarVariable(corredor);
                                        } 
                                        gc.fillOval(corredor.verticeCentro.getX()-80, corredor.verticeCentro.getY(), 20, 20);

                                        try {
                                            Thread.sleep(3000);
                                            actualizar();
                                        } catch (InterruptedException ex) {
                                            Logger.getLogger(Sistema.class.getName()).log(Level.SEVERE, null, ex);
                                        }

                                    }
                                    b=false;
                                    corredors.clear();
                                    variables.clear();
                                    
                                }
                            
                        
                        });
                        hilo.start();
                        
                        
                        //hilo.stop();
                        
                    }
                
                }else{
                    Alert alert = new Alert(AlertType.WARNING);
                    alert.setTitle("Error");
                    alert.setHeaderText("Cuidado");
                    alert.setContentText("No se ha encontrado el inicio o el fin");

                    alert.showAndWait();
                    b=false;
                }
            }else{
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Error");
                alert.setHeaderText("Cuidado");
                alert.setContentText("No se ha encontrado ninguna figuras");

                alert.showAndWait();
                b=false;
            }
        }
        b=false;
    }
    
    private boolean existeVariable(String nombre){
        for (Variable variable : variables) {
            if(variable.nombre.equals(nombre)){
                return true;
            }
        }
        return false;
    }
    private void agregarVariable(Figura figura){
        
        
        
        if(figura.tipo == TipoF.ENTRADA){
            if(figura.texto.matches("[A-Za-z1-9]+=.+")){
                if(!existeVariable(figura.texto.split("=")[0])){
                    area.appendText(figura.texto.split("=")[0]+"⟵  "+evaluarAritmetica(figura.texto.split("=")[1], variables)+" \n");
                    variables.add(new Variable(figura.texto.split("=")[0],evaluarAritmetica(figura.texto.split("=")[1], variables)+""));
                }else{
                    for (int i = 0; i < variables.size(); i++) {
                        if(variables.get(i).nombre.equals(figura.texto.split("=")[0])){
                            variables.get(i).setValor(evaluarAritmetica(figura.texto.split("=")[1], variables)+"");
                        }
                    }

                }
            }else if(figura.texto.matches("[A-Za-z1-9]+")){
                area.appendText("\"el valor de la variable "+figura.texto+" es: "+valorAritmetico(figura.texto, variables)+"\" \n");
            }
        }else if(figura.tipo == TipoF.PROCESO){
            for (int i = 0; i < variables.size(); i++) {
                if(variables.get(i).nombre.equals(figura.texto.split("=")[0])){
                    area.appendText(figura.texto.split("=")[0]+"⟵"+evaluarAritmetica(figura.texto.split("=")[1], variables)+" \n");
                    variables.get(i).setValor(evaluarAritmetica(figura.texto.split("=")[1], variables)+"");
                }
            }
        }
    }
    
    @FXML
    private void dibujarFlujo(ActionEvent event) {
        
        gc.setStroke(Color.BLACK);
  
        b=true;
        canvas.setOnMouseClicked((MouseEvent event1) -> {
            double x1 = event1.getX();
            double y1 = event1.getY();
            Vertice vertice1;
            if((vertice1=buscarConexion(x1,y1).getVerticeCentro())!=null){
                canvas.setOnMouseClicked(null);
                canvas.setOnMouseClicked((MouseEvent event2) -> {
                    if(b){
                        double puntox1=vertice1.getX();
                        double puntoy1=vertice1.getY();
                        double x2 = event2.getX();
                        double y2 = event2.getY();
                        Vertice vertice2;
                      
                        if((vertice2 = buscarConexion(x2,y2).getVerticeCentro())!=null){
                            double puntox2 = vertice2.getX();
                            double puntoy2 = vertice2.getY();
                            Flujo flujo = new Flujo(TipoF.FLUJO);
                            flujo.getVertices().add(new Vertice(puntox1,puntoy1));
                            flujo.getVertices().add(new Vertice(puntox2,puntoy2));
                            flujo.setEstado(true);
                            
                            agregarFiguras(flujo);
                            
                            
                            flujo.calcularVertices(figuras);

                            if(flujoValido(flujo)){
                                
                                figuras.add(flujo);
                                flujo.dibujar(gc);
                            }
                            
                            actualizar();
                            b=false;
                            canvas.setOnMouseClicked(null);
                        }
                        
                    }


                });
            
            }
            
        });
           
    }
    
    public void agregarFiguras(Flujo flujo){
        
        for (int i = 0; i < figuras.size(); i++) {
            if(!(figuras.get(i) instanceof Flujo)){
                if(flujo.getVertices().get(0).distancia(figuras.get(i).getVerticeCentro())==0){
                    flujo.padre = figuras.get(i);
                    flujo.indexPadre = i;
                    break;
                }
            }
        }
        for (int i = 0; i < figuras.size(); i++) {
            if(!(figuras.get(i) instanceof Flujo)){
                if(flujo.getVertices().get(1).distancia(figuras.get(i).getVerticeCentro())==0){
                    flujo.hijo = figuras.get(i);
                    flujo.indexHijo = i;
                    break;
                }
            }
        }
   
    }
    public boolean estaConectado(double x, double y){
        if(!figuras.isEmpty()) {
            for (Figura figura : figuras) {
                if(!(figura instanceof Flujo)){
                    if(x<=figura.getVertices().get(2).getX()){
                        if(x>=figura.getVertices().get(0).getX()){
                           if(y<=figura.getVertices().get(2).getY()){
                                if(y>=figura.getVertices().get(0).getY()){
                                    return true;
                                }
                            } 
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    
    public Figura buscarConexion(double x, double y){
        if(!figuras.isEmpty()) {
            for (Figura figura : figuras) {
                if(!(figura instanceof Flujo)){
                    if(x<=figura.getVertices().get(2).getX()){
                        if(x>=figura.getVertices().get(0).getX()){
                           if(y<=figura.getVertices().get(2).getY()){
                                if(y>=figura.getVertices().get(0).getY()){
                                    return figura;
                                }
                            } 
                        }
                    }
                }
            }
        }
        
        return null;
    }
    public boolean comprobarPosicion(double x1,double y1,double x2,double y2,double x3,double y3,double x4,double y4 ){
        if (!figuras.isEmpty()){
            for (int i = 0; i < figuras.size(); i++) {
                if(figuras.get(i).tipo!= TipoF.FLUJO && figuras.get(i).tipo==TipoF.PROCESO){
                    if(figuras.get(i).getVerticeCentro().getX()-50<=x1 && figuras.get(i).getVerticeCentro().getX()+50>=x1 && figuras.get(i).getVerticeCentro().getY()-25<=y1 && figuras.get(i).getVerticeCentro().getY()+25>=y1){
                        return false;
                    }else{
                        if(figuras.get(i).getVerticeCentro().getX()-50<=x2 && figuras.get(i).getVerticeCentro().getX()+50>=x2 && figuras.get(i).getVerticeCentro().getY()-25<=y2 && figuras.get(i).getVerticeCentro().getY()+25>=y2){
                            return false;
                        }else{              
                            if(figuras.get(i).getVerticeCentro().getX()-50<=x3 && figuras.get(i).getVerticeCentro().getX()+50>=x3 && figuras.get(i).getVerticeCentro().getY()-25<=y3 && figuras.get(i).getVerticeCentro().getY()+25>=y3){
                                return false;
                            }else{
                                if(figuras.get(i).getVerticeCentro().getX()-50<=x4 && figuras.get(i).getVerticeCentro().getX()+50>=x4 && figuras.get(i).getVerticeCentro().getY()-25<=y4 && figuras.get(i).getVerticeCentro().getY()+25>=y4){
                                    return false;
                                }
                            }
                        }
                    }
                
                }
                if(figuras.get(i).tipo!= TipoF.FLUJO && figuras.get(i).tipo==TipoF.INICIO ||figuras.get(i).tipo!= TipoF.FLUJO && figuras.get(i).tipo==TipoF.FIN ){
                    if(figuras.get(i).getVerticeCentro().getX()-90<=x1 && figuras.get(i).getVerticeCentro().getX()+90>=x1 && figuras.get(i).getVerticeCentro().getY()-25<=y1 && figuras.get(i).getVerticeCentro().getY()+25>=y1){
                        return false;
                    }else{
                        if(figuras.get(i).getVerticeCentro().getX()-90<=x2 && figuras.get(i).getVerticeCentro().getX()+90>=x2 && figuras.get(i).getVerticeCentro().getY()-25<=y2 && figuras.get(i).getVerticeCentro().getY()+25>=y2){
                            return false;
                        }else{              
                            if(figuras.get(i).getVerticeCentro().getX()-90<=x3 && figuras.get(i).getVerticeCentro().getX()+90>=x3 && figuras.get(i).getVerticeCentro().getY()-25<=y3 && figuras.get(i).getVerticeCentro().getY()+25>=y3){
                                return false;
                            }else{
                                if(figuras.get(i).getVerticeCentro().getX()-90<=x4 && figuras.get(i).getVerticeCentro().getX()+90>=x4 && figuras.get(i).getVerticeCentro().getY()-25<=y4 && figuras.get(i).getVerticeCentro().getY()+25>=y4){
                                    return false;
                                }
                            }
                        }
                    }
                }
                if(figuras.get(i).tipo!=TipoF.FLUJO && figuras.get(i).tipo==TipoF.ENTRADA){
                    if(figuras.get(i).getVerticeCentro().getX()-67<=x1 && figuras.get(i).getVerticeCentro().getX()+67>=x1 && figuras.get(i).getVerticeCentro().getY()-25<=y1 && figuras.get(i).getVerticeCentro().getY()+25>=y1){
                        return false;
                    }else{
                        if(figuras.get(i).getVerticeCentro().getX()-67<=x2 && figuras.get(i).getVerticeCentro().getX()+67>=x2 && figuras.get(i).getVerticeCentro().getY()-25<=y2 && figuras.get(i).getVerticeCentro().getY()+25>=y2){
                            return false;
                        }else{              
                            if(figuras.get(i).getVerticeCentro().getX()-67<=x3 && figuras.get(i).getVerticeCentro().getX()+67>=x3 && figuras.get(i).getVerticeCentro().getY()-25<=y3 && figuras.get(i).getVerticeCentro().getY()+25>=y3){
                                return false;
                            }else{
                                if(figuras.get(i).getVerticeCentro().getX()-67<=x4 && figuras.get(i).getVerticeCentro().getX()+67>=x4 && figuras.get(i).getVerticeCentro().getY()-25<=y4 && figuras.get(i).getVerticeCentro().getY()+25>=y4){
                                    return false;
                                }
                            }
                        }
                    }
                }
                if(figuras.get(i).tipo!=TipoF.FLUJO && figuras.get(i).tipo==TipoF.DOCUMENTACION){
                    if(figuras.get(i).getVerticeCentro().getX()-50<=x1 && figuras.get(i).getVerticeCentro().getX()+50>=x1 && figuras.get(i).getVerticeCentro().getY()-25<=y1 && figuras.get(i).getVerticeCentro().getY()+75>=y1){
                        return false;
                    }else{
                        if(figuras.get(i).getVerticeCentro().getX()-50<=x2 && figuras.get(i).getVerticeCentro().getX()+50>=x2 && figuras.get(i).getVerticeCentro().getY()-25<=y2 && figuras.get(i).getVerticeCentro().getY()+75>=y2){
                            return false;
                        }else{              
                            if(figuras.get(i).getVerticeCentro().getX()-50<=x3 && figuras.get(i).getVerticeCentro().getX()+50>=x3 && figuras.get(i).getVerticeCentro().getY()-25<=y3 && figuras.get(i).getVerticeCentro().getY()+75>=y3){
                                return false;
                            }else{
                                if(figuras.get(i).getVerticeCentro().getX()-50<=x4 && figuras.get(i).getVerticeCentro().getX()+50>=x4 && figuras.get(i).getVerticeCentro().getY()-25<=y4 && figuras.get(i).getVerticeCentro().getY()+75>=y4){
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
            return true;
        }
        return true;
    }
    
        @FXML
    private void dibujarDesicion(ActionEvent event) throws IOException {
        
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Crear proceso");
        dialog.setContentText("Introduzca el texto:");


        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().equals(" ") && !result.get().equalsIgnoreCase(" ")){
           
   
            if(contar(result.get())>12){
                powerUp = 80;
                boolean a= true;
            }
            
            b=true;
            canvas.setOnMouseClicked((MouseEvent event2) -> {
            double p1 = event2.getX();
            double p2 = event2.getY();
            
            redimensionCanvas(p1,p2);
            
            
            if(b){
                
                double x = event2.getX();
                double y = event2.getY();
                double x1 = x ;
                double y1 = y - 50;
                double x2 = x-50;
                double y2 = y ;
                double x3 = x ;
                double y3 = y+50;
                double x4 = x + 50;
                double y4 = y;
                
                
                Desicion desicion = new Desicion(TipoF.DESICION);
                    desicion.setVerticeCentro(new Vertice(x,y));
                    desicion.getVertices().add(new Vertice(x1, y1));
                    desicion.getVertices().add(new Vertice(x2, y2));
                    desicion.getVertices().add(new Vertice(x3, y3));
                    desicion.getVertices().add(new Vertice(x4, y4));
                    //desicion.calcularConexiones();
                    desicion.texto = result.get();
                    figuras.add(desicion);
                    desicion.dibujar(gc);
                /*if(comprobarPosicion(x1,y1,x2,y2,x3,y3,x4,y4)==true){
                   
                    //Proceso proceso = new Proceso( TipoF.PROCESO);
                    
                    Desicion desicion = new Desicion(TipoF.DESICION);
                    desicion.setVerticeCentro(new Vertice(x,y));
                    desicion.getVertices().add(new Vertice(x1, y1));
                    desicion.getVertices().add(new Vertice(x2, y2));
                    desicion.getVertices().add(new Vertice(x3, y3));
                    desicion.getVertices().add(new Vertice(x4, y4));
                    desicion.calcularConexiones();
                    desicion.texto = result.get();
                    figuras.add(desicion);
                    desicion.dibujar(gc);
                    
                }else{
        
                    Alert alert = new Alert(AlertType.WARNING);
                    alert.setTitle("Error");
                    alert.setHeaderText("Cuidado");
                    alert.setContentText("Ya se encuentra una figura creada aquÃ­ ");
                    alert.showAndWait();
                }*/
                actualizar();
                b=false;
                powerUp=0;
                canvas.setOnMouseClicked(null);
            }  
        });
            
        } else {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Error");
            alert.setHeaderText("cuidado");
            alert.setContentText("Debes ingresar un texto");

            alert.showAndWait();
        }
        
    }
    
    
    @FXML
    private void dibujarEntrada(ActionEvent event) throws IOException {
        
        
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Crear Entrada");
        dialog.setContentText("Introduzca el texto:");

        
        Optional<String> result = dialog.showAndWait();
        
        System.out.println(""+result.get());
        if (result.get().matches("[A-Za-z]+=.+") || result.get().matches("[A-Za-z]+")){
                b= true;
                canvas.setOnMouseClicked((MouseEvent event2) -> {
                double p1 = event2.getX();
                double p2 = event2.getY();
                redimensionCanvas(p1,p2);
                if(b){  
                    double x = event2.getX();
                    double y = event2.getY();
                    double x1 = x - 33;
                    double y1 = y - 25;
                    double x2 = x + 67;
                    double y2 = y - 25;
                    double x3 = x + 33;
                    double y3 = y + 25;
                    double x4 = x - 67;
                    double y4 = y + 25;
                    /***
                     * por si la figura se sale por la izquierda y
                     * por si la figura se sale por la parte superior
                     */
                    if(x4<0){
                        x=55;
                        x1=x1-x4;
                        x2=x2-x4;
                        x3=x3-x4;
                        x4=0;
                    }
                    if(y1<0){
                        y=25;
                        y3=y3-y1;
                        y4=y3;
                        y2=0;
                        y1=0;
                    }

                    Entrada entrada = new Entrada(TipoF.ENTRADA);
                    entrada.setVerticeCentro(new Vertice(x,y));
                    entrada.getVertices().add(new Vertice(x1,y1));
                    entrada.getVertices().add(new Vertice(x2,y2));
                    entrada.getVertices().add(new Vertice(x3,y3));
                    entrada.getVertices().add(new Vertice(x4,y4));
                    entrada.calcularConexiones();
                    entrada.texto = result.get();
                    entrada.setEstado(true);
                    figuras.add(entrada);
                    entrada.dibujar(gc);

                    actualizar();

                    b=false;
                    canvas.setOnMouseClicked(null);

                }       
            });
            
        }else {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Error");
            alert.setHeaderText("cuidado");
            alert.setContentText("El texto ingresado es incorrecto debe ser del estilo 'algo' = 'algo' o solamente 'algo'");

            alert.showAndWait();
        }
        
        
     
    }
    private ArrayList<Figura> auxi = new ArrayList<>();
    @FXML
    private void limpiar(ActionEvent event) {
        b=true;
        canvas.setOnMouseClicked((MouseEvent event2)->{
            double px=event2.getX();
            double py=event2.getY();
            
            if(!figuras.isEmpty()){
                if(b){
                    for (int i = 0; i < figuras.size(); i++) {
                        if(figuras.get(i).getVertices().get(0).getX()<= px &&
                            figuras.get(i).getVertices().get(1).getX()>=px &&
                            figuras.get(i).getVertices().get(0).getY()<= py &&
                            figuras.get(i).getVertices().get(2).getY()>=py &&
                            figuras.get(i).getTipo()==TipoF.PROCESO ){
                            figuras.get(i).setEstado(false);
                        }else{
                            if(figuras.get(i).getVertices().get(0).getX()-20<= px &&
                            figuras.get(i).getVertices().get(1).getX()+20>=px &&
                            figuras.get(i).getVertices().get(0).getY()<= py &&
                            figuras.get(i).getVertices().get(2).getY()>=py &&
                            figuras.get(i).getTipo()==TipoF.INICIO ){
                               figuras.get(i).setEstado(false);
                            }else if(figuras.get(i).getVertices().get(0).getX()-20<= px &&
                            figuras.get(i).getVertices().get(1).getX()+20>=px &&
                            figuras.get(i).getVertices().get(0).getY()<= py &&
                            figuras.get(i).getVertices().get(2).getY()>=py &&
                            figuras.get(i).getTipo()==TipoF.FIN ){
                                figuras.get(i).setEstado(false);
                                
                            }else{
                                
                                if(figuras.get(i).getVertices().get(0).getX()<= px &&
                                figuras.get(i).getVertices().get(1).getX()>=px &&
                                figuras.get(i).getVertices().get(0).getY()<= py &&
                                figuras.get(i).getVertices().get(2).getY()+20>=py &&
                                figuras.get(i).getTipo()==TipoF.ENTRADA){
                                    
                                }else{
                                    if(figuras.get(i).getVertices().get(0).getX()<= px &&
                                    figuras.get(i).getVertices().get(1).getX()>=px &&
                                    figuras.get(i).getVertices().get(0).getY()<= py &&
                                    figuras.get(i).getVertices().get(2).getY()>=py &&
                                    figuras.get(i).getTipo()==TipoF.DOCUMENTACION){
                                
                                    }else{
                                    
                                    
                                        if(figuras.get(i) instanceof Flujo && 
                                        figuras.get(i).getVerticeCentro().getX()-5 <= px &&
                                        figuras.get(i).getVerticeCentro().getX()+5 >= px &&
                                        figuras.get(i).getVerticeCentro().getY()-5 <= py &&
                                        figuras.get(i).getVerticeCentro().getY()+5 >= py &&
                                        figuras.get(i).getTipo()==TipoF.FLUJO){
                                            figuras.get(i).setEstado(false);
                                        }else{
                                            if(figuras.get(i).getEstado()!=false){
                                                
                                                figuras.get(i).setEstado(true);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    /*for (int i = 0; i < figuras.size(); i++) {
                        System.out.println(""+figuras.get(i).getEstado());
                    }*/
                    /**figuras.clear();
                    for (int i = 0; i < auxi.size(); i++) {
                        figuras.add(auxi.get(i));
                    }**/
                    for (Figura figura : figuras) {
                        if(figura instanceof Flujo && figura.getEstado()!=false){
                            ((Flujo)figura).calcularVertices(figuras);
                        }
                    }
                    gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                    
                    actualizar();
                    
                    canvas.setOnMouseClicked(null);
                }
                b=false;
            }
            else{
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Error");
                alert.setHeaderText("cuidado");
                alert.setContentText("No has dibujo una figura, intenta crear una antes");

                alert.showAndWait();
            }
        
        
          
    });
    }
    private void actualizar(){
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        System.out.println("Actualizando");

        
        for (Figura figura : figuras) {
            //if(figura instanceof Flujo && figura.getEstado()!=false){
            if(figura instanceof Flujo && figura.getEstado()!=false){
                
                ((Flujo) figura).calcularVertices(figuras);
                figura.dibujar(gc);
            }
            
        }
        for (Figura figura : figuras) {
            if(!(figura instanceof Flujo) && figura.getEstado()!=false){
            //if(!(figura instanceof Flujo )&& figura.getEstado()!=false){
                
                figura.dibujar(gc);
            }
        }
        
        for (int i = 0; i < figuras.size(); i++) {
            if(figuras.get(i).getEstado()!=false){
                auxi.add(figuras.get(i));
            }
            
        }
        figuras.clear();
        for (int i = 0; i < auxi.size(); i++) {
            figuras.add(auxi.get(i));
        }
        auxi.clear();
        
    }
        

        
      

    
    
    @FXML
    private void dibujarInicio(ActionEvent event) throws IOException {
        b= true;
        
        canvas.setOnMouseClicked((MouseEvent event2) -> {
            double p1 = event2.getX();
            double p2 = event2.getY();
            if(existeInicio()==false || existeFin()==false){
                redimensionCanvas(p1,p2);    
                if(b){
                    double x = event2.getX();
                    double y = event2.getY();
                    double x1 = x - 50;
                    double y1 = y - 25;
                    double x2 = x + 50;
                    double y2 = y - 25;
                    double x3 = x + 50;
                    double y3 = y + 25;
                    double x4 = x - 50;
                    double y4 = y + 25;

                    if(x1-20<0){
                        x=70;
                        x2=x2-x1+20;
                        x3=x3-x1+20;
                        x4=x4-x1+20;
                        x1=20;
                    }
                    if(y1<0){
                        y=25;
                        y3=y3-y1;
                        y4=y4-y1;
                        y1=0;
                        y2=0;
                    }


                    if(existeInicio()==false){
                        Inicio inicio = new Inicio(TipoF.INICIO);
                        inicio.setVerticeCentro(new Vertice(x,y));
                        inicio.getVertices().add(new Vertice(x1,y1));
                        inicio.getVertices().add(new Vertice(x2,y2));
                        inicio.getVertices().add(new Vertice(x3,y3));
                        inicio.getVertices().add(new Vertice(x4,y4));
                        inicio.calcularConexiones();
                        //se debe validar la diferencia entre inicio y fin
                        inicio.texto = "INICIO";
                        inicio.setEstado(true);
                        figuras.add(inicio);
                        inicio.dibujar(gc);
                        

                    }else if(existeFin() == false){
                        Inicio inicio = new Inicio(TipoF.FIN);
                        inicio.setVerticeCentro(new Vertice(x,y));
                        inicio.getVertices().add(new Vertice(x1,y1));
                        inicio.getVertices().add(new Vertice(x2,y2));
                        inicio.getVertices().add(new Vertice(x3,y3));
                        inicio.getVertices().add(new Vertice(x4,y4));
                        inicio.calcularConexiones();
                        //se debe validar la diferencia entre inicio y fin
                        inicio.texto = "FIN";
                        inicio.setEstado(true);
                        figuras.add(inicio);
                        inicio.dibujar(gc);
                    }
                    
                    actualizar();
                    b=false;
                    canvas.setOnMouseClicked(null);

                }
            }
        });
    
       
    }
    
    public boolean existeInicio(){
        if(!figuras.isEmpty()){
            for (Figura figura : figuras) {
                if(figura.getTipo()==TipoF.INICIO){
                    return true;
                }
            }
        }
        return false;
    }
    public boolean existeFin(){
        if(!figuras.isEmpty()){
            for (Figura figura : figuras) {
                if(figura.getTipo()==TipoF.FIN){
                    return true;
                }
            }
        }
        return false;
    }
    
    @FXML
    private void dibujarDocumento(ActionEvent event) throws IOException {
        
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Crear documento");
        dialog.setContentText("Introduzca el texto:");

    
        Optional<String> result = dialog.showAndWait();
        if (result.get().matches("[A-Za-z]+")){
            
            b= true;
        
            canvas.setOnMouseClicked((MouseEvent event2) -> {
            double p1 = event2.getX();
            double p2 = event2.getY();
            redimensionCanvas(p1,p2);   
            if(b){
               
                double x = event2.getX();
                double y = event2.getY();
                double x1 = x - 50;
                double y1 = y - 25;
                double x2 = x + 50;
                double y2 = y - 25;
                double x3 = x + 50;
                double y3 = y + 25;
                double x4 = x - 50;
                double y4 = y + 25;
                
                /***
                 * por si la figura a crear se crea muy arriba o muy a la izquierda 
                 * o la combinacion de las dos anteriores
                 */
                if(x1<0){
                    x=50;
                    x2=x2-x1;
                    x3=x3-x1;
                    x4=0;
                    x1=0;
                }
                if(y1<0){
                    y=25;
                    y3=y3-y1;
                    y4=y4-y1;        
                    y2=0;
                    y1=0;
                }
                
                Documento documento = new Documento(TipoF.DOCUMENTACION);
                documento.setVerticeCentro(new Vertice(x,y));
                documento.getVertices().add(new Vertice(x1,y1));
                documento.getVertices().add(new Vertice(x2,y2));
                documento.getVertices().add(new Vertice(x3,y3));
                documento.getVertices().add(new Vertice(x4,y4));
                documento.texto = result.get();
                documento.calcularConexiones();
                documento.setEstado(true);
                figuras.add(documento);
                documento.dibujar(gc);
                
                actualizar();
                
                
                    
                b=false;
                canvas.setOnMouseClicked(null);
                
            

            }      
          
        });
        }else {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Error");
            alert.setHeaderText("cuidado");
            alert.setContentText("El formato del texto ingresado es incorrecto");

            alert.showAndWait();
        }
        
    }
    
    boolean moviendo = false;
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        mover();
        
        
        
        gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        
        ap.setBackground(Background.EMPTY);
        
        
    }    
    
    
    public void redimensionCanvas(double p1, double p2){
        if(p1+60>canvas.getWidth()){
            canvas.setWidth(canvas.getWidth()+50);
            if(p2+60>canvas.getHeight()){canvas.setHeight(canvas.getHeight()+50);
                }
            }else if (p2+51>canvas.getHeight()) {
                canvas.setHeight(canvas.getHeight()+50);
                
        }
    }
    
    public boolean flujoValido(Flujo flujo){
        
        if(flujo.padre.getVerticeCentro().distancia(flujo.hijo.getVerticeCentro())==0){
            return false;
        }
        
        if(flujo.padre.getTipo() == TipoF.FIN || flujo.hijo.getTipo() == TipoF.INICIO){
            return false;
        }
        for (Figura figura : figuras) {
            if(figura instanceof Flujo){
                if((flujo.vertices.get(0).distancia(figura.getVertices().get(0))==0 && flujo.vertices.get(1).distancia(figura.getVertices().get(1))==0) || 
                        (flujo.vertices.get(0).distancia(figura.getVertices().get(1))==0 && flujo.vertices.get(1).distancia(figura.getVertices().get(0))==0)){
                    return false;
                }if(flujo.padre.getVerticeCentro().distancia(((Flujo) figura).padre.getVerticeCentro())==0
                        || flujo.hijo.getVerticeCentro().distancia(((Flujo) figura).hijo.getVerticeCentro())==0 ){
                    return false;
                }

            }
        }
        
        
        return true;
    
    }
    
    
    public int contar(String cadena){
        char[] arrayChar = cadena.toCharArray();
        int cant=0;
        for(int i=0; i<arrayChar.length; i++){
            cant++;
        }

        return cant;
    }

    public ArrayList<Figura> getFiguras() {
        return figuras;
    }

    public void setFiguras(ArrayList<Figura> figuras) {
        this.figuras = figuras;
    }

    public boolean evaluarAritmetica(String expresion, ArrayList<Variable> variables){
        if(expresion.matches("[0-9]+")){
            return true;
            
        }else if(expresion.matches("[A-Za-z]+")){
            if(valorAritmetico(expresion, variables)){
                return true;
            }
            
        }else if(expresion.matches("\\(.+\\)")){
           expresion = expresion.replaceFirst("[(]", "");
           int index = expresion.lastIndexOf(")");
           expresion = expresion.substring(0, index)+expresion.substring(index+1);
           return evaluarAritmetica(expresion, variables);
           
        }else if(expresion.matches("-((-[0-9]+)|([0-9]+))")){
            return true;
            
        }else if(expresion.matches("\\d+\\*.+")){
            int index = expresion.indexOf("*");
            return true && evaluarAritmetica(expresion.substring(index+1),variables);
        
        }else if(expresion.matches("\\d+\\/.+")){
            int index = expresion.indexOf("/");
            return true && evaluarAritmetica(expresion.substring(index+1),variables);
        
        }else if(expresion.matches("\\d+\\+.+")){
            int index = expresion.indexOf("+");
            return true && evaluarAritmetica(expresion.substring(index+1),variables);
        
        }else if(expresion.matches("\\d+\\-.+")){
            int index = expresion.indexOf("-");
            return true && evaluarAritmetica(expresion.substring(index+1),variables);
            
        }else if(expresion.matches("[A-Za-z1-9]+\\*.+")){
            int index = expresion.indexOf("*");
            return true && evaluarAritmetica(expresion.substring(index+1),variables);
        
        
        }else if(expresion.matches("[A-Za-z1-9]+\\/.+")){
            int index = expresion.indexOf("/");
            return true && evaluarAritmetica(expresion.substring(index+1),variables);
        
        
        }else if(expresion.matches("[A-Za-z1-9]+\\+.+")){
            int index = expresion.indexOf("+");
            return true && evaluarAritmetica(expresion.substring(index+1),variables);
        
        
        }else if(expresion.matches("[A-Za-z1-9]+\\-.+")){
            int index = expresion.indexOf("-");
            return true && evaluarAritmetica(expresion.substring(index+1),variables);
        
        
        }else if(expresion.matches("\\(.+\\)\\*\\(.+\\)")){
            int index = expresion.indexOf("*");
            return true && evaluarAritmetica(expresion.substring(index+1),variables);
            
        }else if(expresion.matches("\\(.+\\)\\/\\(.+\\)")){
            int index = expresion.indexOf("/");
            return true && evaluarAritmetica(expresion.substring(index+1),variables);
            
        }else if(expresion.matches("\\(.+\\)\\+\\(.+\\)")){
            int index = expresion.indexOf("+");
            return true && evaluarAritmetica(expresion.substring(index+1),variables);
            
        }
        else if(expresion.matches("\\(.+\\)-\\(.+\\)")){
            int index = expresion.indexOf("-");
            return true && evaluarAritmetica(expresion.substring(index+1),variables);
            
        }else{
            return false;
        }
        return false;
        
       
    }
    public boolean valorAritmetico(String expresion, ArrayList<Variable> variables){
        for (Variable variable : variables) {
            if (variable.nombre.equals(expresion)) {
                return true;
            }
            
        }
        // se deja esto asi por mientras
        return false;
    }
    public double operarExpresion(String expresion, ArrayList<Variable> variables){
        String variableAuxiliar= expresion.split("=")[0];
        String operacion= expresion.split("=")[1];
        for (int i = 0; i < expresion.length(); i++) {
            
        }
        return 0;
    }
    
}