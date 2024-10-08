// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2024T2, Assignment 3
 * Name:
 * Username:
 * ID:
 */

import ecs100.*;
import java.util.*;

/**
 * Simulation of a Hospital ER
 * <p>
 * The hospital has a collection of Departments, including the ER department, each of which has
 *  and a treatment room.
 * <p>
 * When patients arrive at the hospital, they are immediately assessed by the
 *  triage team who determine the priority of the patient and (unrealistically) a sequence of treatments 
 *  that the patient will need.
 * <p>
 * The simulation should move patients through the departments for each of the required treatments,
 * finally discharging patients when they have completed their final treatment.
 * <p>
 *  READ THE ASSIGNMENT PAGE!
 */

public class HospitalERCompl {

    /**
     * The map of the departments.
     * The names of the departments should be "ER", "X-Ray", "MRI", "UltraSound" and "Surgery"
     * The maximum patients should be 8 for "ER", 3 for "X-Ray", 1 for "MRI", 2 for "UltraSound" and
     * 3 for "Surgery"
     */

    private Map<String, Department> departments = new HashMap<String, Department>();
    private Queue<Patient> waitingRoom = new ArrayDeque<Patient>();
    private final Set<Patient> treatmentRoom = new HashSet<Patient>();
    public boolean inPriorityQue = false;

    // Copy the code from HospitalERCore and then modify/extend to handle multiple departments
    /*# YOUR CODE HERE */
    int numFinTreatment = 0;
    int totalTimeInWaiting = 0;
    int tot1treated = 0;
    int tot1TimeWaiting = 0;

    // Fields for the simulation
    private boolean running = false;
    private int time = 0; // The simulated time - the current "tick"
    private int delay = 300;  // milliseconds of real time for each tick


    /**
     * Reset the simulation:
     * stop any running simulation,
     * reset the waiting and treatment rooms
     * reset the statistics.
     */
    public void reset(boolean usePriorityQueue) {
        running = false;
        UI.sleep(2 * delay);  // to make sure that any running simulation has stopped

        time = 0;           // set the "tick" to zero.
        // reset the waiting room, the treatment room, and the statistics.
        /*# YOUR CODE HERE */
        waitingRoom.clear();
        treatmentRoom.clear();
        if (usePriorityQueue) {
            waitingRoom = new PriorityQueue<>();
            inPriorityQue = true;
        } else {
            waitingRoom = new ArrayDeque<>();
            inPriorityQue = false;
        }
        //statistics clear
        numFinTreatment = 0;
        totalTimeInWaiting = 0;
        tot1treated = 0;
        tot1TimeWaiting = 0;
        //visual clear
        UI.clearGraphics();
        UI.clearText();
    }

    /**
     * Main loop of the simulation
     */

    public void run() {
        if (running) {
            return;
        } // don't start simulation if already running one!
        running = true;
        initialiseDepartments();

        while (running) {
            time++;
            for(Department department : departments.values()) {
                department.processPatient(this);
            }
            Patient newPatient = PatientGenerator.getNextPatient(time);
            if (newPatient != null) {
                departments.get("ER").addPatient(newPatient);
                UI.println(time + ": Arrived: " + newPatient);
            }
            redraw();
            UI.sleep(delay);
        }
        reportStatistics();
    }

    // Additional methods used by run() (You can define more of your own)

    /**
     * Report summary statistics about all the patients that have been discharged.
     */

    public void reportStatistics() {
        double averageTotalP1WaitingTime = tot1treated == 0 ? 0 : (double) tot1TimeWaiting / tot1treated;
        double avgTotalWaitingTime = numFinTreatment == 0 ? 0 : (double) totalTimeInWaiting / numFinTreatment;
        UI.println("Average waiting time: " + avgTotalWaitingTime);
        UI.println("Average waiting time for priority 1 patients: " + averageTotalP1WaitingTime);
        UI.println("Total Treated: " + numFinTreatment);
        UI.println("Total priority 1 treated: " + tot1treated);
    }



    public void initialiseDepartments(){
        // create each new department with values,
        // add each department to the map of departments
        Department ER = new Department("ER",8,inPriorityQue);
        Department XRay = new Department("X-Ray",3,inPriorityQue);
        Department MRI = new Department("MRI",1,inPriorityQue);
        Department UltraSound = new Department("UltraSound",2,inPriorityQue);
        Department Surgery = new Department("Surgery",3,inPriorityQue);
        departments.put("ER", ER);
        departments.put("X-Ray", XRay);
        departments.put("MRI", MRI);
        departments.put("UltraSound", UltraSound);
        departments.put("Surgery", Surgery);
    }

    public void nextDepartment(Patient p) {
        if (p.allTreatmentsCompleted()) {
            discharge(p);
        } else {
            String nextDepartmentName = p.getCurrentDepartment();
            Department nextDepartment = departments.get(nextDepartmentName);
            if (nextDepartment != null) {
                nextDepartment.addPatient(p);
            } else {
                UI.println("Error: Department not found for " + nextDepartmentName);
            }
        }
    }


    public void discharge(Patient p){
        numFinTreatment++;
        totalTimeInWaiting += p.getTotalWaitingTime();
        UI.println(p.getTotalWaitingTime());
        if (p.getPriority() ==1){
            tot1treated++;
            tot1TimeWaiting += p.getTotalWaitingTime();
        }
        UI.println("Discharged: " + p);
    }


    // METHODS FOR THE GUI AND VISUALISATION

    /**
     * Set up the GUI: buttons to control simulation and sliders for setting parameters
     */
    public void setupGUI() {
        UI.addButton("Reset (Queue)", () -> {
            this.reset(false);
        });
        UI.addButton("Reset (Pri Queue)", () -> {
            this.reset(true);
        });
        UI.addButton("Start", () -> {
            if (!running) {
                run();
            }
        });   //don't start if already running!
        UI.addButton("Pause & Report", () -> {
            running = false;
        });
        UI.addSlider("Speed", 1, 400, (401 - delay), (double val) -> {
            delay = (int) (401 - val);
        });
        UI.addSlider("Av arrival interval", 1, 50, PatientGenerator.getArrivalInterval(),
                PatientGenerator::setArrivalInterval);
        UI.addSlider("Prob of Pri 1", 1, 100, PatientGenerator.getProbPri1(),
                PatientGenerator::setProbPri1);
        UI.addSlider("Prob of Pri 2", 1, 100, PatientGenerator.getProbPri2(),
                PatientGenerator::setProbPri2);
        UI.addButton("Quit", UI::quit);
        UI.setWindowSize(1000, 600);
        UI.setDivider(0.5);
    }

    /**
     * Redraws all the patients and the state of the simulation
     */
    public void redraw() {
        UI.clearGraphics();
        UI.setFontSize(14);
        UI.drawString("Treating Patients", 5, 15);
        UI.drawString("Waiting Queues", 200, 15);
        UI.drawLine(0, 32, 400, 32);
        double y = 80;
        for(Department department : departments.values()) {
            department.redraw(y);
            UI.drawLine(0,y+2,400,y+2);
            y+=50;
        }
    }


    /**
     * main:  Construct a new HospitalERCore object, setting up the GUI, and resetting
     */
    public static void main(String[] arguments) {
        HospitalERCompl er = new HospitalERCompl();
        er.setupGUI();
        er.reset(false);   // initialise with an ordinary queue.
    }
}

