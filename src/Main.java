import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.swing.JOptionPane;


public class Main {
    static String filepath = System.getProperty("user.home") + "/Desktop";

    public static void main(String[] args) {
        showUserOptions();
        JOptionPane.showMessageDialog(null, "Gracias por usar mi programa");
    }

    private static void showUserOptions() {
        mainLoop:
        while (true) {
            String option = SafeInput.showInputDialog("""
                    Sistema de gestion de notas escolares:
                    
                    1 - Estudiantes
                    2 - Profesores
                    N - Salir
                    """);
            switch(option) {
                case "1":
                    showStudentPortal();
                    break;
                case "2":
                    showMainMenu();
                    break;
                case "N":
                case "n":
                    break mainLoop;
                default:
                    SafeInput.showMessage("Opción no válida");
            }
        }
    }

    private static void showStudentPortal() {
        int studentCount = Students.students.size();
        int signatureCount = Signatures.signatures.size();
        if (studentCount == 0 || signatureCount == 0) {
            JOptionPane.showMessageDialog(null,
                    "Debe poseer estudiantes y materias registrados antes de ver las notas");
            return;
        }
        String cedula = SafeInput.getString("Seleccione su número de cédula para ver sus notas: ",
                "Debe ingresar un número de cédula incluido valido", "[0-9]+");
        Optional<Student> selectedStudent = Students.findStudentByCedula(cedula);
        if (selectedStudent.isEmpty()) {
            SafeInput.showMessage("Estudiante no encontrado");
            return;
        }
        StringBuilder output = new StringBuilder("Estudiante: " + selectedStudent.get().name + "\n" +
                                                 "Cedula: " + selectedStudent.get().cedula + "\n\n");
        List<ScoreLoad> filteredScoreLoads = ScoreLoads.getFilteredScoreLoadsByStudent(selectedStudent.get());
        for (ScoreLoad filteredScoreLoad : filteredScoreLoads) {
            Optional<Signature> signature = Signatures.findSignature(filteredScoreLoad.signatureId);
            if (signature.isEmpty()) continue;
            output.append("Materia: ").append(signature.get().name).append("\n");
            output.append("Corte 1: ").append(filteredScoreLoad.term1).append("\n");
            output.append("Corte 2: ").append(filteredScoreLoad.term2).append("\n");
            output.append("Corte 3: ").append(filteredScoreLoad.term3).append("\n");
            output.append("Definitiva: ").append(filteredScoreLoad.avg).append("\n\n");
        }
        JOptionPane.showMessageDialog(null,
                output);
    }


    private static void showMainMenu() {
        String option;
        loop:
        while (true) {
            option = SafeInput.showInputDialog("""
                    Profesores
                                                
                    Estudiantes:
                    1 - Ingresar estudiante     2 - Mostrar estudiante
                    3 - Actualizar estudiante   4 - Eliminar estudiante
                                                
                    Materias:
                    5 - Ingresar materia        6 - Mostrar materias
                    7 - Actualizar materia      8 - Eliminar materia
                                                
                    Ingreso Notas:
                    9 - Ingreso masivo          10 -Ingreso por estudiante
                    11 -Ingreso por materia
                                                
                    Mostrar Notas:
                    12 -Por estudiante          13 -Por materia
                                            
                    Base de datos:
                    14 -Guardar datos           15 -Cargar datos
                                                
                    N/n - Salir
                    """
            );
            switch (option) {
                case "1":
                    showEnterStudent();
                    break;
                case "2":
                    showStudents();
                    break;
                case "3":
                    showUpdateStudent();
                    break;
                case "4":
                    showDeleteStudent();
                    break;
                case "5":
                    showEnterSignature();
                    break;
                case "6":
                    showSignatures();
                    break;
                case "7":
                    showUpdateSignature();
                    break;
                case "8":
                    showDeleteSignature();
                    break;
                case "9":
                    showMasiveScoreInput();
                    break;
                case "10":
                    showPerStudentScoreInput();
                    break;
                case "11":
                    showPerSignatureScoreInput();
                    break;
                case "12":
                    showScoresPerStudent();
                    break;
                case "13":
                    showScoresPerSignature();
                    break;
                case "14":
                    saveData();
                    break;
                case "15":
                    loadData();
                    break;
                case "n":
                case "N":
                    break loop;
                default:
                    JOptionPane.showMessageDialog(null, "Opcion no permitida");
            }
        }
    }

    private static void loadData() {
        try {
            List<String> files = new ArrayList<>();
            StringBuilder output = new StringBuilder();
            int selectedFile;
            for (final File fileEntry : Objects.requireNonNull((new File(filepath)).listFiles())) {
                files.add(fileEntry.getName());
            }
            for (int i = 0; i < files.size(); i++) {
                int j = i + 1;
                output.append(j).append(" - ").append(files.get(i)).append("\n");
            }
            output.append("Seleccione un archivo de la lista");
            selectedFile = SafeInput.getIntWithRange(output.toString(), "Seleccion no valida",
                    "[0-9]+", 0, files.size());
            String filename = files.get(selectedFile - 1);
            Path path = Paths.get(filepath, filename);

            FileInputStream fi = new FileInputStream(path.toString());
            ObjectInputStream oi = new ObjectInputStream(fi);

            Students.students = (List<Student>) oi.readObject();
            Signatures.signatures = (List<Signature>) oi.readObject();
            ScoreLoads.scoreLoads = (List<ScoreLoad>) oi.readObject();

            oi.close();
            fi.close();
            SafeInput.showMessage("Datos cargados con éxito");
        } catch (FileNotFoundException e) {
            SafeInput.showMessage("Archivo no encontrado");
        } catch (IOException e) {
            SafeInput.showMessage("Error inicializando canal");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void saveData() {
        String filename = SafeInput.getString("Indique el nombre del archivo, n para cancelar", "Nombre no válido",
                "[a-zA-Z0-9]+");
        if (Objects.equals(filename, "n") || Objects.equals(filename, "N")) return;
        try {
            Path path = Paths.get(filepath, filename);
            FileOutputStream f = new FileOutputStream(path.toString());
            ObjectOutputStream o = new ObjectOutputStream(f);
            o.writeObject(Students.students);
            o.writeObject(Signatures.signatures);
            o.writeObject(ScoreLoads.scoreLoads);
            o.close();
            f.close();
            SafeInput.showMessage("Sesión guardada con éxito");
        } catch (FileNotFoundException e) {
            SafeInput.showMessage("Archivo no encontrado");
        } catch (IOException e) {
            SafeInput.showMessage("Error inicializando canal");
        }
    }

    private static void showPerSignatureScoreInput() {
        int studentCount = Students.students.size();
        int signatureCount = Signatures.signatures.size();
        if (studentCount == 0 || signatureCount == 0) {
            JOptionPane.showMessageDialog(null,
                    "Debe poseer estudiantes y materias registrados antes de ingresar las notas");
            return;
        }
        String confirm;
        int selectedSignatureId = SafeInput.getSignature(Students.showStudents() +
                                                         "\nSeleccione el número de la materia para ingresar sus notas: ",
                "Debe ingresar un número de materia incluido en la lista");
        Optional<Signature> selectedSignature = Signatures.findSignature(selectedSignatureId);
        if (selectedSignature.isEmpty()) return;
        Signature signature = selectedSignature.get();
        students:
        for (int i = 0; i < studentCount; i++) {
            Student student = Students.students.get(i);
            while (true) {
                int term1 = SafeInput.getIntWithRange(
                        "Estudiante: " + student.name + "\n" +
                        "Materia: " + signature.name + "\n" +
                        "Ingrese la nota del primer corte",
                        "La nota debe ser un numero entre 0 y 20",
                        "[0-9]+", 0, 20);
                int term2 = SafeInput.getIntWithRange(
                        "Estudiante: " + student.name + "\n" +
                        "Materia: " + signature.name + "\n" +
                        "Ingrese la nota del segundo corte",
                        "La nota debe ser un numero entre 0 y 20",
                        "[0-9]+", 0, 20);
                int term3 = SafeInput.getIntWithRange(
                        "Estudiante: " + student.name + "\n" +
                        "Materia: " + signature.name + "\n" +
                        "Ingrese la nota del tercer corte",
                        "La nota debe ser un numero entre 0 y 20",
                        "[0-9]+", 0, 20);

                confirm = SafeInput.showInputDialog("Carga de notas ingresada: \n" +
                                                    "Estudiante: " + student.name + "\n" +
                                                    "Materia: " + signature.name + "\n" +
                                                    "Primer corte: " + term1 + "\n" +
                                                    "Segundo corte: " + term2 + "\n" +
                                                    "Tercer corte: " + term3 + "\n" +
                                                    "¿Desea realizar esta carga de notas? S/N\n" +
                                                    "Marque 'E' para pasar al siguiente estudiante\n" +
                                                    "Marque 'R' para regresar al menu inicial\n"
                );

                if (!Objects.equals(confirm, "n") && !Objects.equals(confirm, "N")) {
                    ScoreLoads.addScoreLoad(signature.id, student.id, term1, term2, term3);
                    JOptionPane.showMessageDialog(null,
                            "Notas almacenada exitosamente");
                    if (Objects.equals(confirm, "e") || Objects.equals(confirm, "E")) continue students;
                    if (Objects.equals(confirm, "r") || Objects.equals(confirm, "R")) break students;
                    break;
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Notas no almacenada. Vuelva a intentarlo");
                }
            }
        }
    }

    private static void showPerStudentScoreInput() {
        int studentCount = Students.students.size();
        int signatureCount = Signatures.signatures.size();
        if (studentCount == 0 || signatureCount == 0) {
            JOptionPane.showMessageDialog(null,
                    "Debe poseer estudiantes y materias registrados antes de ingresar las notas");
            return;
        }
        String confirm;
        int selectedStudentId = SafeInput.getStudent(Students.showStudents() +
                                                     "\nSeleccione el número del estudiante para ingresar sus notas: ",
                "Debe ingresar un número de estudiante incluido en la lista");
        Optional<Student> selectedStudent = Students.findStudent(selectedStudentId);
        if (selectedStudent.isEmpty()) return;
        Student student = selectedStudent.get();
        signatures:
        for (int j = 0; j < signatureCount; j++) {
            Signature signature = Signatures.signatures.get(j);
            while (true) {
                int term1 = SafeInput.getIntWithRange(
                        "Estudiante: " + student.name + "\n" +
                        "Materia: " + signature.name + "\n" +
                        "Ingrese la nota del primer corte",
                        "La nota debe ser un numero entre 0 y 20",
                        "[0-9]+", 0, 20);
                int term2 = SafeInput.getIntWithRange(
                        "Estudiante: " + student.name + "\n" +
                        "Materia: " + signature.name + "\n" +
                        "Ingrese la nota del segundo corte",
                        "La nota debe ser un numero entre 0 y 20",
                        "[0-9]+", 0, 20);
                int term3 = SafeInput.getIntWithRange(
                        "Estudiante: " + student.name + "\n" +
                        "Materia: " + signature.name + "\n" +
                        "Ingrese la nota del tercer corte",
                        "La nota debe ser un numero entre 0 y 20",
                        "[0-9]+", 0, 20);

                confirm = SafeInput.showInputDialog("Carga de notas ingresada: \n" +
                                                    "Estudiante: " + student.name + "\n" +
                                                    "Materia: " + signature.name + "\n" +
                                                    "Primer corte: " + term1 + "\n" +
                                                    "Segundo corte: " + term2 + "\n" +
                                                    "Tercer corte: " + term3 + "\n" +
                                                    "¿Desea realizar esta carga de notas? S/N\n" +
                                                    "Marque 'E' para pasar al siguiente estudiante\n" +
                                                    "Marque 'M' para pasar a la siguiente materia\n");


                if (!Objects.equals(confirm, "n") && !Objects.equals(confirm, "N")) {
                    ScoreLoads.addScoreLoad(signature.id, student.id, term1, term2, term3);
                    JOptionPane.showMessageDialog(null,
                            "Notas almacenadas exitosamente");
                    if (Objects.equals(confirm, "m") || Objects.equals(confirm, "M")) continue signatures;
                    if (Objects.equals(confirm, "r") || Objects.equals(confirm, "R")) break signatures;
                    break;
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Notas no almacenada. Vuelva a intentarlo");
                }
            }
        }
    }

    private static void showScoresPerStudent() {
        int studentCount = Students.students.size();
        int signatureCount = Signatures.signatures.size();
        if (studentCount == 0 || signatureCount == 0) {
            JOptionPane.showMessageDialog(null,
                    "Debe poseer estudiantes y materias registrados antes de ver las notas");
            return;
        }
        int selectedStudentId = SafeInput.getStudent(Students.showStudents() +
                                                     "\nSeleccione el número del estudiante para ver sus notas: ",
                "Debe ingresar un número de estudiante incluido en la lista");
        Optional<Student> selectedStudent = Students.findStudent(selectedStudentId);
        if (selectedStudent.isEmpty()) return;
        StringBuilder output = new StringBuilder("Estudiante: " + selectedStudent.get().name + "\n" +
                                                 "Cedula: " + selectedStudent.get().cedula + "\n\n");
        List<ScoreLoad> filteredScoreLoads = ScoreLoads.getFilteredScoreLoadsByStudent(selectedStudent.get());
        for (ScoreLoad filteredScoreLoad : filteredScoreLoads) {
            Optional<Signature> signature = Signatures.findSignature(filteredScoreLoad.signatureId);
            if (signature.isEmpty()) continue;
            output.append("Materia: ").append(signature.get().name).append("\n");
            output.append("Corte 1: ").append(filteredScoreLoad.term1).append("\n");
            output.append("Corte 2: ").append(filteredScoreLoad.term2).append("\n");
            output.append("Corte 3: ").append(filteredScoreLoad.term3).append("\n");
            output.append("Definitiva: ").append(filteredScoreLoad.avg).append("\n\n");
        }
        JOptionPane.showMessageDialog(null,
                output);
    }

    private static void showScoresPerSignature() {
        int studentCount = Students.students.size();
        int signatureCount = Signatures.signatures.size();
        if (studentCount == 0 || signatureCount == 0) {
            JOptionPane.showMessageDialog(null,
                    "Debe poseer estudiantes y materias registrados antes de ver las notas");
            return;
        }
        int selectedSignatureId = SafeInput.getSignature(Signatures.showSignatures() +
                                                         "\nSeleccione el número de la materia para ver sus notas: ",
                "Debe ingresar un número de materia incluido en la lista");
        Optional<Signature> selectedSignature = Signatures.findSignature(selectedSignatureId);
        if (selectedSignature.isEmpty()) return;
        StringBuilder output = new StringBuilder("Materia: " + selectedSignature.get().name + "\n\n");
        List<ScoreLoad> filteredScoreLoads = ScoreLoads.getFilteredScoreLoadsBySignature(selectedSignatureId);
        for (ScoreLoad filteredScoreLoad : filteredScoreLoads) {
            Optional<Student> student = Students.findStudent(filteredScoreLoad.studentId);
            if (student.isEmpty()) continue;
            output.append("Estudiante: ").append(student.get().name).append("\n");
            output.append("Corte 1: ").append(filteredScoreLoad.term1).append("\n");
            output.append("Corte 2: ").append(filteredScoreLoad.term2).append("\n");
            output.append("Corte 3: ").append(filteredScoreLoad.term3).append("\n");
            output.append("Definitiva: ").append(filteredScoreLoad.avg).append("\n\n");
        }
        JOptionPane.showMessageDialog(null,
                output);
    }

    private static void showMasiveScoreInput() {
        int studentCount = Students.students.size();
        int signatureCount = Signatures.signatures.size();
        if (studentCount == 0 || signatureCount == 0) {
            JOptionPane.showMessageDialog(null,
                    "Debe poseer estudiantes y materias registrados antes de ingresar las notas");
            return;
        }
        String confirm;
        students:
        for (int i = 0; i < studentCount; i++) {
            Student student = Students.students.get(i);
            signatures:
            for (int j = 0; j < signatureCount; j++) {
                Signature signature = Signatures.signatures.get(j);
                while (true) {
                    int term1 = SafeInput.getIntWithRange(
                            "Estudiante: " + student.name + "\n" +
                            "Materia: " + signature.name + "\n" +
                            "Ingrese la nota del primer corte",
                            "La nota debe ser un numero entre 0 y 20",
                            "[0-9]+", 0, 20);
                    int term2 = SafeInput.getIntWithRange(
                            "Estudiante: " + student.name + "\n" +
                            "Materia: " + signature.name + "\n" +
                            "Ingrese la nota del segundo corte",
                            "La nota debe ser un numero entre 0 y 20",
                            "[0-9]+", 0, 20);
                    int term3 = SafeInput.getIntWithRange(
                            "Estudiante: " + student.name + "\n" +
                            "Materia: " + signature.name + "\n" +
                            "Ingrese la nota del tercer corte",
                            "La nota debe ser un numero entre 0 y 20",
                            "[0-9]+", 0, 20);

                    confirm = SafeInput.showInputDialog("Carga de notas ingresada: \n" +
                                                        "Estudiante: " + student.name + "\n" +
                                                        "Materia: " + signature.name + "\n" +
                                                        "Primer corte: " + term1 + "\n" +
                                                        "Segundo corte: " + term2 + "\n" +
                                                        "Tercer corte: " + term3 + "\n" +
                                                        "¿Desea realizar esta carga de notas? S/N\n" +
                                                        "Marque 'M' para pasar a la siguiente materia\n" +
                                                        "Marque 'E' para pasar al siguiente estudiante\n" +
                                                        "Marque 'R' para regresar al menu inicial\n"
                    );

                    if (!Objects.equals(confirm, "n") && !Objects.equals(confirm, "N")) {
                        ScoreLoads.addScoreLoad(signature.id, student.id, term1, term2, term3);
                        JOptionPane.showMessageDialog(null,
                                "Notas almacenadas exitosamente");
                        if (Objects.equals(confirm, "m") || Objects.equals(confirm, "M")) continue signatures;
                        if (Objects.equals(confirm, "e") || Objects.equals(confirm, "E")) continue students;
                        if (Objects.equals(confirm, "r") || Objects.equals(confirm, "R")) break students;
                        break;
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "Notas no almacenada. Vuelva a intentarlo");
                    }
                }
            }
        }
    }

    private static void showStudents() {
        int studentCount = Students.students.size();
        if (studentCount == 0) {
            JOptionPane.showMessageDialog(null,
                    "Debe poseer estudiantes ingresados");
            return;
        }
        JOptionPane.showMessageDialog(null,
                Students.showStudents());
    }

    private static void showSignatures() {
        int signatureCount = Signatures.signatures.size();
        if (signatureCount == 0) {
            JOptionPane.showMessageDialog(null,
                    "Debe poseer materias ingresadas");
            return;
        }
        JOptionPane.showMessageDialog(null,
                Signatures.showSignatures());
    }

    private static void showDeleteSignature() {
        int signatureCount = Signatures.signatures.size();
        if (signatureCount == 0) {
            JOptionPane.showMessageDialog(null,
                    "Debe poseer materias ingresadas");
            return;
        }
        int signatureToDelete = SafeInput.getSignature(Signatures.showSignatures() +
                                                       "\nSeleccione el número de la materia que desea eliminar: ",
                "Debe ingresar un número de materia incluido en la lista");
        Signatures.removeSignature(signatureToDelete);
        JOptionPane.showMessageDialog(null, "Materia eliminada");
    }

    private static void showDeleteStudent() {
        int studentCount = Students.students.size();
        if (studentCount == 0) {
            JOptionPane.showMessageDialog(null,
                    "Debe poseer estudiantes ingresados");
            return;
        }
        int studentToDelete = SafeInput.getStudent(Students.showStudents() +
                                                   "\nSeleccione el número del estudiante que desea eliminar: ",
                "Debe ingresar un número de estudiante incluido en la lista");
        Students.removeStudent(studentToDelete);
        JOptionPane.showMessageDialog(null, "Estudiante eliminado");
    }

    private static void showUpdateSignature() {
        int signatureCount = Signatures.signatures.size();
        if (signatureCount == 0) {
            JOptionPane.showMessageDialog(null,
                    "Debe poseer materias ingresadas");
            return;
        }
        int signatureId = SafeInput.getSignature(Signatures.showSignatures() +
                                                 "\nSeleccione el número de la materia que desea actualizar: ",
                "Debe ingresar un número de materia incluido en la lista");
        Optional<Signature> signatureToUpdate = Signatures.findSignature(signatureId);
        String name = SafeInput.getString("Ingrese el nuevo nombre de la materia: ",
                "El nombre de la materia solo puede poseer letras", "[a-zA-Z ]+");

        signatureToUpdate.ifPresent(signature -> {
            Signatures.updateSignature(signature, name);
            JOptionPane.showMessageDialog(null, "Materia actualizada");
        });
    }

    private static void showUpdateStudent() {
        int studentCount = Students.students.size();
        if (studentCount == 0) {
            JOptionPane.showMessageDialog(null,
                    "Debe poseer estudiantes ingresados");
            return;
        }
        int studentId = SafeInput.getStudent(Students.showStudents() +
                                             "\nSeleccione el número del estudiante que desea actualizar: ",
                "Debe ingresar un número de estudiante incluido en la lista");
        Optional<Student> studentToUpdate = Students.findStudent(studentId);
        String name = SafeInput.getString("Ingrese el nombre del estudiante: ",
                "El nombre del estudiante solo puede poseer letras", "[a-zA-Z ]+");
        String cedula = SafeInput.getString("Ingrese la cédula del estudiante: ",
                "La cédula del estudiante solo puede poseer letras", "[0-9]+");
        String confirm = SafeInput.showInputDialog("Estudiante ingresado: \n" +
                                                   "Nombre: " + name + "\n" +
                                                   "Cedula: " + cedula + "\n" +
                                                   "¿Desea actualizar este estudiante? S/N");
        if (Objects.equals(confirm, "s") || Objects.equals(confirm, "S")) {
            studentToUpdate.ifPresent(student -> {
                Students.updateStudent(student, name, cedula);
                JOptionPane.showMessageDialog(null, "Estudiante actualizado");
            });
        } else {
            JOptionPane.showMessageDialog(null,
                    "No se pudo actualizar el estudiante");
        }
    }

    private static void showEnterSignature() {
        String name = SafeInput.getString("Ingrese el nombre de la materia: ",
                "El nombre de la materia solo puede poseer letras", "[a-zA-Z ]+");
        Signatures.createSignature(name);
        JOptionPane.showMessageDialog(null, "Materia almacenada");
    }

    private static void showEnterStudent() {
        String name = SafeInput.getString("Ingrese el nombre del estudiante: ",
                "El nombre del estudiante solo puede poseer letras", "[a-zA-Z ]+");
        String cedula = SafeInput.getString("Ingrese la cédula del estudiante: ",
                "La cédula del estudiante solo puede poseer letras", "[0-9]+");
        String confirm = SafeInput.showInputDialog("Estudiante ingresado: \n" +
                                                   "Nombre: " + name + "\n" +
                                                   "Cedula: " + cedula + "\n" +
                                                   "¿Desea almacenar este estudiante? S/N");
        if (Objects.equals(confirm, "s") || Objects.equals(confirm, "S")) {
            Students.createStudent(name, cedula);
            JOptionPane.showMessageDialog(null, "Estudiante almacenado");
        } else {
            JOptionPane.showMessageDialog(null,
                    "No se pudo almacenar el estudiante");
        }
    }
}

class SafeInput {
    public static String showInputDialog(String message) {
        String input = JOptionPane.showInputDialog(null, message);
        if (input == null) {
            input = "n";
        }
        return input;
    }

    public static void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    public static String getString(String message, String correctMessage, String validator) {
        boolean invalid = true;
        String input = "";
        while (invalid) {
            input = SafeInput.showInputDialog(message);
            invalid = !input.matches(validator);
            if (invalid) {
                JOptionPane.showMessageDialog(null, correctMessage);
            }
        }
        return input;
    }

    public static int getIntWithRange(String message, String correctMessage, String validator, int min, int max) {
        boolean invalid = true;
        String input;
        int response = 0;
        while (invalid) {
            input = SafeInput.showInputDialog(message);
            invalid = !input.matches(validator);
            if (invalid) {
                JOptionPane.showMessageDialog(null, correctMessage);
            }
            response = Integer.parseInt(input);
            if (response < min || response > max) {
                JOptionPane.showMessageDialog(null, correctMessage);
                invalid = true;
            }
        }
        return response;
    }

    public static int getStudent(String message, String correctMessage) {
        boolean invalid = true;
        int studentId = 0;
        String input;
        while (invalid) {
            input = SafeInput.showInputDialog(message);
            studentId = Integer.parseInt(input);
            invalid = !input.matches("[0-9]+") || !Students.doesStudentExist(studentId);
            if (invalid) {
                JOptionPane.showMessageDialog(null, correctMessage);
            }
        }
        return studentId;
    }

    public static int getSignature(String message, String correctMessage) {
        boolean invalid = true;
        int signatureId = 0;
        String input;
        while (invalid) {
            input = SafeInput.showInputDialog(message);
            signatureId = tryParse(input);
            invalid = !input.matches("[0-9]+") || !Signatures.doesSignatureExist(signatureId);
            if (invalid) {
                JOptionPane.showMessageDialog(null, correctMessage);
            }
        }
        return signatureId;
    }

    public static Integer tryParse(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}

class Students {
    public static List<Student> students = new ArrayList<>();

    public static String showStudents() {
        StringBuilder output = new StringBuilder("Estudiantes: \n");
        for (int i = 0; i < (long) students.size(); i++) {
            output.append(String.format("%d - %s \n CI:%s\n", students.get(i).id, students.get(i).name,
                    students.get(i).cedula));
        }
        return output.toString();
    }

    public static void createStudent(String name, String cedula) {
        Student newStudent = new Student(students.size() + 1, name, cedula);
        students.add(newStudent);
    }

    public static void updateStudent(Student oldStudent, String name, String cedula) {
        Student newStudent = new Student(oldStudent.id, name, cedula);
        students.set(students.indexOf(oldStudent), newStudent);
    }

    public static void removeStudent(int studentId) {
        Optional<Student> studentToDelete = students.stream().filter(s -> s.id == studentId).findAny();
        studentToDelete.ifPresent(student -> students.remove(student));
    }

    public static boolean doesStudentExist(int studentId) {
        return students.stream().anyMatch(s -> s.id == studentId);
    }

    public static Optional<Student> findStudent(int studentId) {
        return students.stream().filter(s -> s.id == studentId).findFirst();
    }

    public static Optional<Student> findStudentByCedula(String cedula) {
        return students.stream().filter(s -> Objects.equals(s.cedula, cedula)).findFirst();
    }

}

class Student implements Serializable {
    public int id;
    public String name;
    public String cedula;

    public Student(int id, String name, String cedula) {
        this.id = id;
        this.name = name;
        this.cedula = cedula;
    }
}

class Signatures {
    public static List<Signature> signatures = new ArrayList<>();

    public static String showSignatures() {
        StringBuilder output = new StringBuilder("Materias: \n");
        for (int i = 0; i < (long) signatures.size(); i++) {
            output.append(signatures.get(i).id).append(" ").append(signatures.get(i).name).append("\n");
        }
        return output.toString();
    }

    public static void createSignature(String name) {
        Signature newSignature = new Signature(signatures.size() + 1, name);
        signatures.add(newSignature);
    }

    public static void updateSignature(Signature oldSignature, String name) {
        Signature newSignature = new Signature(oldSignature.id, name);
        signatures.set(signatures.indexOf(oldSignature), newSignature);
    }

    public static void removeSignature(int signatureId) {
        Optional<Signature> signatureToDelete = signatures.stream().filter(s -> s.id == signatureId).findAny();
        signatureToDelete.ifPresent(signature -> signatures.remove(signature));
    }

    public static boolean doesSignatureExist(int signatureId) {
        return signatures.stream().
                anyMatch(s -> s.id == signatureId);
    }

    public static Optional<Signature> findSignature(int signatureId) {
        return signatures.stream().filter(s -> s.id == signatureId).findFirst();
    }
}

class ScoreLoads {
    public static List<ScoreLoad> scoreLoads = new ArrayList<>();

    public static void addScoreLoad(int signatureId, int studentId, int term1, int term2, int term3) {
        Optional<ScoreLoad> previousScoreLoad = scoreLoads.stream().filter(sl ->
                sl.signatureId == signatureId && sl.signatureId == studentId).findFirst();

        ScoreLoad scoreLoad = new ScoreLoad(signatureId, studentId, term1, term2, term3);

        if (previousScoreLoad.isEmpty()) {
            scoreLoads.add(scoreLoad);
            return;
        }
        ScoreLoad previous = previousScoreLoad.get();
        scoreLoads.set(scoreLoads.indexOf(previous), scoreLoad);
    }

    public static List<ScoreLoad> getFilteredScoreLoadsByStudent(Student student) {
        return scoreLoads.stream().filter(sl -> sl.studentId == student.id).toList();
    }

    public static List<ScoreLoad> getFilteredScoreLoadsBySignature(int signatureId) {
        return scoreLoads.stream().filter(sl -> sl.signatureId == signatureId).toList();
    }
}

class ScoreLoad implements Serializable {
    public int signatureId;
    public int studentId;
    public int term1;
    public int term2;
    public int term3;
    public int avg;

    public ScoreLoad(int signatureId, int studentId, int term1, int term2, int term3) {
        this.studentId = signatureId;
        this.signatureId = studentId;
        this.term1 = term1;
        this.term2 = term2;
        this.term3 = term3;
        this.avg = (int) (this.term1 * 0.3 + this.term2 * 0.3 + this.term3 * 0.4);
    }
}

class Signature implements Serializable {
    public int id;
    public String name;

    public Signature(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
