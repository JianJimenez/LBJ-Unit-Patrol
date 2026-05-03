package lbj_patrol;

import java.util.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.awt.event.*;

public class LBJ_Patrol {
	
	private static final Scanner scan = new Scanner(System.in);
	private static ArrayList<Unit> MASTERLIST = new ArrayList<>();
	private static Set<Integer> ROOMS = new HashSet<>();
    private static final String DB_FILE = "DATABASE.txt";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
	public static void main(String[] args) throws IOException {
		System.out.println("=== LBJ Unit Patrol: Your Go-To Lab Buddy ===");
        System.out.println("[1] GUI Mode");
        System.out.println("[2] CLI Mode");
        System.out.print("Select mode [1 or 2]: ");
        
        int mode = scan.nextInt();
        scan.nextLine();
        
        DATA.loadData();
        
        if (mode == 1) {
        	SwingUtilities.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new MainGUI();
            });
        } else {
            User.login();
        }
        DATA.saveData();

	}
	
	//ALGORITHM
	public static class DATA {
		public static void loadData() throws IOException{getData();}
		public static void saveData() throws IOException {setData();}
		private static void getData() throws IOException{
			System.out.println("Loading Data from " + DB_FILE + "...");
			File DATABASE = new File(DB_FILE);
			
			if (!DATABASE.exists()) {
				System.out.println("DATABASE not found. Starting from new list.");
				MASTERLIST.add( new Unit(101, 1, LocalDate.now(),	true, 	false, 	false, 	false,	"-", 		"-"						));
				MASTERLIST.add(	new Unit(101, 2, LocalDate.now(),	true, 	true, 	false, 	false,	"Pending", "PC does not turn on"	));
				MASTERLIST.add(	new Unit(101, 3, LocalDate.now(),	false,	false, 	true, 	false,	"#DeadPC", "PC does not turn on"	));
				MASTERLIST.add(	new Unit(101, 4, LocalDate.now(),	false, 	false, 	true, 	true,	"#DeadPC", "PC does not turn on"	));
				MASTERLIST.add(	new Unit(101, 5, LocalDate.now(),	true, 	false, 	false, 	false,	"-", 		"-"						));
				return;
			}
			try (BufferedReader br = new BufferedReader(new FileReader(DATABASE))){
				String line;
				while ((line = br.readLine()) != null) {
					String[] parts = line.split(",");
					if (parts.length >= 9) {
						Unit u = new 	Unit(
											Integer.parseInt	(parts[0]),
											Integer.parseInt	(parts[1]),
											LocalDate.parse		(parts[2]),
											Boolean.parseBoolean(parts[3]),
											Boolean.parseBoolean(parts[4]),
											Boolean.parseBoolean(parts[5]),
											Boolean.parseBoolean(parts[6]),
											parts[7],
											parts[8]
										);
						MASTERLIST.add(u);
						ROOMS.add(u.getRoomNo());
					}
				}
			} catch (IOException e) {
				System.out.println("Error loading file: " + e.getMessage());
			}
		}
		
		private static void setData() throws IOException {
			Collections.sort(MASTERLIST);
			System.out.println("Saving Data to " + DB_FILE + "...");
			try (PrintWriter pw = new PrintWriter(new FileWriter(DB_FILE))) {
				for (Unit u : MASTERLIST) {
					pw.println(u.toDB());
				}
			} catch (IOException e) {
				System.out.println("Error saving file: " + e.getMessage());
			}
		}
		
		public static void printData(ArrayList<Unit> list) {
			if (list.isEmpty()) {
				System.out.println("There are no Units in this Set\n");
			} else {
				for (Unit u : list) {
					u.printUnit();
				}
			}
		}
		public static void printIndexed(ArrayList<Unit> list) {
			if (list.isEmpty()) {
				System.out.println("There are no Units in this Set\n");
			} else {
				for (int i = 0; i < list.size(); i++) {
					System.out.print("[" + (i+1) + "] ");
					list.get(i).printUnit();
				}
			}
		}
		public static int find(ArrayList<Unit> set, int roomNo, int pcNo) {
			int key1 = roomNo;
			int key2 = pcNo;
			return bSearch(set, key1, key2, 0, set.size()-1);
		}
		// BINARY SEARCH
		private static int bSearch(ArrayList<Unit> set, int key1, int key2, int left, int right) {
			if (left > right) {
				return -1;
			}
			int mid = left + (right - left) / 2;
			Unit target = set.get(mid);
			
			int targetRoom = target.getRoomNo();
			int targetPC = target.getPcNo();
			
			if 		(targetRoom > key1) return bSearch(set, key1, key2, left, mid - 1);
			else if (targetRoom < key1) return bSearch(set, key1, key2, mid + 1, right);
			else {
				if (targetPC > key2)	return bSearch(set, key1, key2, left, mid - 1);
				if (targetPC < key2)	return bSearch(set, key1, key2, mid + 1, right);
				else 					return mid;
			}
			
	
		}
	}
	//SET THEORY AND ALGORITHM
	public static class SET {
		//SET THEORY AND LINEAR SEARCH
		public static ArrayList<Unit> generateIntersection(ArrayList<Unit> superset, boolean available, boolean reported, boolean error, boolean repair){
			ArrayList<Unit> subset = new ArrayList<>();
			for (Unit u: superset) {
				boolean 	match = true;
				
							if		(	available == !u.isAvailable()	) match = false;
							else if	(	reported == !u.isReported()		) match = false;
							else if	(	error == !u.isError()			) match = false;
							else if	(	repair == !u.isRepair()			) match = false;
				if(match) {
					subset.add(u);
				}
			}
			return subset;
		}
		public static ArrayList<Unit> generateUnion(ArrayList<Unit> superset, boolean available, boolean reported, boolean error, boolean repair){
			ArrayList<Unit> subset = new ArrayList<>();
			for (Unit u: superset) {
				boolean 	match = false;
						
							if		(	available == u.isAvailable()	) match = true;
							else if	(	reported == u.isReported()		) match = true;
							else if	(	error == u.isError()			) match = true;
							else if	(	repair == u.isRepair()			) match = true;
				if(match) {
					subset.add(u);
				}
			}
			return subset;
		}
		
		public static ArrayList<Unit> generateRoom(ArrayList<Unit> superset, int roomNo) {
			ArrayList<Unit> subset = new ArrayList<>();
			for (Unit u: superset) {
				if (roomNo == u.getRoomNo()) {
					subset.add(u);
				}
			}
			return subset;
		}
		public static ArrayList<Integer> listRooms() {
			ArrayList<Integer> list = new ArrayList<Integer>(ROOMS);
			return list;
		}
		public static void printRooms() {
			ArrayList<Integer> list = listRooms();
			for (int i = 1; i <= list.size(); i++) {
				System.out.println("["+i+"] ICT"+list.get(i-1));
			}
		}
		public static void printRoomsBullet() {
			ArrayList<Integer> list = listRooms();
			for (int i : list) {
				System.out.println("> " + i);
			}
		}
 		public static String errorCodes = "\n====General Errors====\r\n\n"
				+ "#Freeze------PC Freezes\r\n"
				+ "#Slow--------PC is slow\r\n"
				+ "#Crash-------PC Crashed\r\n"
				+ "#Power-------PC turns off	\r\n"
				+ "#Dead--------PC Does not turn On\r\n"
				+ "#NoVideo-----Video Display does not work\r\n"
				+ "#NoAudio-----Audio Output does not work\r\n"
				+ "#NoNet-------No Network Connection\r\n"
				+ "\r\n\n"
				+ "====Software Errors====\r\n\n"
				+ "#ErrApp------Application Error \r\n"
				+ "#ErrSoft-----Software Error\r\n"
				+ "#ErrOS-------Operating System Error\r\n"
				+ "#ErrUPD------Windows Update Error\r\n"
				+ "#ErrDisk-----Storage Disk Error\r\n"
				+ "\r\n\n"
				+ "====Hardware Errors====\r\n\n"
				+ "#ErrHard-----Hardware Error\r\n"
				+ "#ErrMouse----Mouse is broken\r\n"
				+ "#ErrKeyB-----Keyboard is broken\r\n"
				+ "#ErrMntr-----Monitor is broken\r\n"
				+ "#ErrUSB------USB ports are broken\r\n"
				+ "";
	}
	public class User {
		public static void login() {
			int choice = -1;
			System.out.println("\n--LOGIN OPTIONS--");
			System.out.println("[1] Student");
			System.out.println("[2] Professor");
			System.out.println("[3] Admin");
			System.out.println("[0] Exit");
			while (true) {
				try {
					System.out.print("Select Login: ");
					choice = scan.nextInt();
					scan.nextLine();
					switch (choice) {
					case 0:		System.out.println("\nThank you for using LBJ Patrol!");	return;
					case 1:		Student.menu();			return;
					case 2:		Professor.login();		return;
					case 3:		Admin.login();			return;
					default:	throw new Exception();
					}
				}
				catch (Exception e) {
					System.out.println("Please input a valid choice.");
					scan.nextLine();
				} 
			}
		}
	}
	private class Student extends User {
		public static void menu() {
			System.out.println("\n--ICTC Rooms--");
			SET.printRooms();
			System.out.println("[0] Return");
			int choice, roomSelect;
			while (true) {
				try {	
					System.out.print("Select Room: ");
					choice = scan.nextInt();
					if (choice == 0) {
						User.login();
						return;
					}
					else if (choice > 0 && choice <= SET.listRooms().size()) {
						roomSelect = SET.listRooms().get(choice-1);
						System.out.println("[SUCCESS] You have selected ICT"+roomSelect);
						Student.room(roomSelect);
						return;
					} 
					else {throw new Exception();}
				} catch (Exception e) {
				System.out.println("[ERROR] Please input a valid choice.");
				scan.nextLine();
				}
			}
		}
		private static void room(int roomSelect) {
			System.out.println("\n--ICT"+roomSelect+"--");
			System.out.println("[1] Report a Unit");
			System.out.println("[2] View Units");
			System.out.println("[0] Return");
			int choice;
			while (true) {
				try {
					System.out.print("Select Option: ");
					choice = scan.nextInt();
					if (choice == 0) {
						Student.menu();
						return;
					} else {
						switch(choice) {
						case 1:		Student.report(roomSelect); 		return;
						case 2: 	Student.view(roomSelect, 1);		return;
						default: 	throw new Exception();
						}
					}
				} catch (Exception e) {
					System.out.println("[ERROR] Please input a valid choice.");
					scan.nextLine();
				}
			}
		}
		private static void report(int roomSelect) {
			ArrayList<Unit> ROOM = SET.generateRoom(MASTERLIST,	roomSelect);
			System.out.println("\n==REPORT PC [0 to Cancel]==");
			for (Unit u: ROOM) {
				System.out.println("> PC-"+u.getPcNo());
			}
			int input;
			String claim;
			while(true) {
				try {
					System.out.print("Enter PC No.: ");
					input = scan.nextInt();
					scan.nextLine();
					if (input == 0) {
						Student.room(roomSelect);
						return;
					}
					int index = DATA.find(ROOM, roomSelect, input);
					if (index == -1) {
						System.out.println("[ERROR] PC Not Found. Please try again.");
					} else {
						int PCSelect = input;
						ROOM.get(index).printUnit();
						if 		(ROOM.get(index).isRepair()) 	System.out.println("This Unit has ongoing Repairs. Please Select Another.");
						else if (ROOM.get(index).isError()) 	System.out.println("This Unit already has an Error. Please Select Another."); 
						else if (ROOM.get(index).isReported()) 	System.out.println("This Unit is already Reported. Please Select Another.");
						else {
							System.out.println("--PC Found. Describe Issue/Error [0 to Cancel]--");
							while (true) {
								System.out.print("Issue: ");
								claim = scan.nextLine();
								if (claim.equals("0")) {
									Student.room(roomSelect);
									return;
								}
								else {
									while (true) {
										try {
											System.out.println("Confirm report? [1 to Confirm, 2 to Retry, 0 to Cancel]");
											System.out.print("Confirm: ");
											input = scan.nextInt();
											scan.nextLine();
											switch (input) {
											case 0:	Student.room(roomSelect);
													return;
											case 2: System.out.print("Issue: ");
													claim = scan.nextLine();
													break;
											case 1:	System.out.println("Report Confirmed.");
													int master = DATA.find(MASTERLIST, roomSelect, PCSelect);
													if (master == -1) System.out.println("[ERROR] Masterlist Error. Please try again later.");
													else {
														MASTERLIST.get(master).setDate(LocalDate.now());
														MASTERLIST.get(master).setReported(true);
														MASTERLIST.get(master).setErrorClaim(claim);
														MASTERLIST.get(master).setErrorCode("Reported");
														MASTERLIST.get(master).printUnit();
														System.out.println("[SUCCESS] Report Successful. Thank you!");
													}
													Student.room(roomSelect);
													return;
											}
										} catch (Exception e) {
											System.out.println("[ERROR] Please input a valid choice.");
											scan.nextLine();
										}
									}
									
								}
							}
						}
					}
				} catch (Exception e) {
					System.out.println("[ERROR] Please input a valid choice.");
					scan.nextLine();
				}
			}
		}
		private static void view(int roomSelect, int display) {
			System.out.println("\n--VIEW ICT"+ roomSelect +"--");
			ArrayList<Unit> ROOM = SET.generateRoom(MASTERLIST,	roomSelect);
			switch(display) {
				case 1: System.out.println("Display: ALL");
						DATA.printData(ROOM);			break;
				case 2:	System.out.println("Display: AVAILABLE");
						ArrayList<Unit> AVAILABLE 	=  SET.generateIntersection(ROOM, true, false, false, false);
						DATA.printData( AVAILABLE); 	break;	
				case 3:	System.out.println("Display: UNAVAILABLE");
						ArrayList<Unit> UNAVAILABLE = SET.generateUnion(ROOM, false, true, true, true);
						DATA.printData(	UNAVAILABLE); 	break;
			}
			System.out.println("--------------");
			System.out.println("SELECT DISPLAY");
			System.out.println("[1] All Units (Default)");
			System.out.println("[2] Available Units");
			System.out.println("[3] Unavailable Units");
			System.out.println("[0] Return");
			int choice;
			while (true) {
				try {
					System.out.print("Select Display: ");
					choice = scan.nextInt();
					if(choice==0) {
						Student.room(roomSelect);
						return;
					} else {
						switch(choice) {
						case 1:	Student.view(roomSelect, choice); return;
						case 2: Student.view(roomSelect, choice); return;
						case 3: Student.view(roomSelect, choice); return;
						default: throw new Exception();
						}
					}
				} catch (Exception e) {
					System.out.println("[ERROR] Please input a valid choice.");
					scan.nextLine();
				}
			}
			
		}
	}
	private class Professor extends User {
		private static final Map<String, String> PROFESSORS = new HashMap<>();
		static {
			PROFESSORS.put("brteodoro","benjamin");
			PROFESSORS.put("lvfajardo", "leanne");
			PROFESSORS.put("jtjimenez", "jian");
			PROFESSORS.put("a", "a");
		}
		public static void login() {
			System.out.println("\n--PROFESSOR LOGIN--");
			System.out.println("Please Enter Your Username and Password [0 to Cancel]");
			while (true) {
				try  {
					System.out.print("Enter Username: ");
					String user = scan.nextLine();
					if (user.equals("0")) {
						User.login();
						return;
					}
					System.out.print("Enter Password: ");
					String pass = scan.nextLine();
					if (pass.equals("0")) {
						User.login();
						return;
					}
					if (PROFESSORS.containsKey(user) && PROFESSORS.get(user).equals(pass)) {
						System.out.println("[SUCCESS] Login Sucessful. Welcome, " + user + "!");
						Professor.menu();
						return;
					} else {
						System.out.println("[ERROR] Invalid Username/Password. Please try again");
					}
				} catch (Exception e) {
					System.out.println("[ERROR] Please input a valid choice.");
					scan.nextLine();
				}
			}
		}
		private static void menu() {				// Change to Private
			System.out.println("\n--ICTC Rooms--");
			SET.printRooms();
			System.out.println("[0] Logout");
			int choice, roomSelect;
			while (true) {
				try {	
					System.out.print("Select Room: ");
					choice = scan.nextInt();
					scan.nextLine();
					if (choice == 0) {
						User.login();
						return;
					}
					else if (choice > 0 && choice <= SET.listRooms().size()) {
						roomSelect = SET.listRooms().get(choice-1);
						System.out.println("[SUCCESS] You have selected ICT"+roomSelect);
						Professor.room(roomSelect);
						return;
					} 
					else {throw new IndexOutOfBoundsException();}
				}catch(IndexOutOfBoundsException e) {
					System.out.println("[ERROR] Please input a valid choice.");
				} catch (Exception e) {
				System.out.println("[ERROR] Please input a valid choice.");
				scan.nextLine();
				}
			}
		}
		private static void room(int roomSelect) {
			ArrayList<Unit> ROOM = SET.generateRoom(MASTERLIST, roomSelect);
			System.out.println("\n--ICT"+roomSelect+"--");
			System.out.println("[1] View Available Units");
			System.out.println("[2] View Reports/Errors/Repairs");
			System.out.println("[0] Return");
			int choice;
			while (true) {
				try {
					System.out.print("Select Option: ");
					choice = scan.nextInt();
					scan.nextLine();
					if (choice == 0) {
						Professor.menu();
						return;
					} else {
						switch(choice) {
						case 1:		Professor.viewAvail(ROOM, roomSelect); 			return;
						case 2: 	Professor.viewUnavail(ROOM, roomSelect);		return;
						default: 	throw new IndexOutOfBoundsException();
						}
					}
				} catch(IndexOutOfBoundsException e) {
					System.out.println("Please input a valid choice.");
				} catch (Exception e) {
					System.out.println("Please input a valid choice.");
					scan.nextLine();
				}
			}
		}
		private static void viewAvail(ArrayList<Unit> room, int roomSelect) {
			System.out.println("\n--ICT"+ roomSelect +" AVAILABLE UNITS--");
			ArrayList<Unit> AVAILABLE = SET.generateIntersection(room, true, false, false, false);
			DATA.printData(AVAILABLE);
			System.out.println("--------------");
			System.out.println("[0] Return");
			int choice;
			while (true) {
				try {
					System.out.print("Select Option: ");
					choice = scan.nextInt();
					if(choice==0) {
						Professor.room(roomSelect);
						return;
					} else {
						throw new Exception();
					}
				} catch (Exception e) {
					System.out.println("[ERROR] Please input a valid choice.");
					scan.nextLine();
				}
			}
		}
		private static void viewUnavail(ArrayList<Unit> room, int roomSelect) {
			System.out.println("\n--ICT"+ roomSelect +" ERRORS/REPORTS/REPAIRS--");
			ArrayList<Unit> UNAVAILABLE = SET.generateUnion(room, false, true, true, true);
			DATA.printIndexed(UNAVAILABLE);
			Unit select;
			int choice;
			while (true) {
				try {	
					System.out.print("View Unit [0 to Return]: ");
					choice = scan.nextInt();
					scan.nextLine();
					if (choice == 0) {
						Professor.room(roomSelect);
						return;
					}
					else if (choice > 0 && choice <= UNAVAILABLE.size()) {
						System.out.print("[SELECT] ");
						select = UNAVAILABLE.get(choice-1);
						System.out.println("You have selected PC-"+select.getPcNo());
						Professor.select(select, room, roomSelect);
						return;
					} 
					else {throw new IndexOutOfBoundsException();}
				} catch (IndexOutOfBoundsException e) {
					System.out.println("[ERROR] Please input a valid choice.");
				} catch (Exception e) {
					System.out.println("[ERROR] Please input a valid choice.");
					scan.nextLine();
				}
			}
		}
		private static void select(Unit u, ArrayList<Unit> room, int roomSelect) {
			System.out.println("\n== [SELECTED]==");
			System.out.println("[ICT" + u.getRoomNo() + " PC-" + u.getPcNo() + "]");
			if (u.isRepair()) {
				System.out.println("This unit is under Repair");
				System.out.println("--OPTIONS--");
				System.out.println("[1] Mark as Done");
				System.out.println("[0] Return");
				int choice;
				while (true){
					try {
						System.out.print("Select Option: ");
						choice = scan.nextInt();
						scan.nextLine();
						if (choice == 0) {
							Professor.viewUnavail(room, roomSelect);
							return;
						}
						switch(choice) {
						case 1: System.out.println("Are you Sure? This will mark the Unit as Repaired.");
								System.out.println("[0 to Cancel, 1 to Confirm]");
								while (true) {
									try {
										System.out.print("Confirm: ");
										choice = scan.nextInt();
										switch(choice) {
										case 0: Professor.viewUnavail(room, roomSelect);
												return;
										case 1:	System.out.println("Confirmed.");
												int master = DATA.find(MASTERLIST, u.getRoomNo(), u.getPcNo());
												if (master == -1) System.out.println("Masterlist Error. Please try again later.");
												else {
													MASTERLIST.get(master).setDate(LocalDate.now());
													MASTERLIST.get(master).setAvailable(true);
													MASTERLIST.get(master).setError(false);
													MASTERLIST.get(master).setRepair(false);
													MASTERLIST.get(master).setReported(false);
													MASTERLIST.get(master).setErrorClaim("-");
													MASTERLIST.get(master).setErrorCode("-");
													MASTERLIST.get(master).printUnit();
													System.out.println("[SUCCESS] Unit is now Available. Thank you!");
												}
												Professor.viewUnavail(room, roomSelect);
												return;
										default: throw new IndexOutOfBoundsException();
										}
									} catch (IndexOutOfBoundsException e) {
										System.out.println("[ERROR] Please input a valid choice.");
									} catch (Exception e) {
										System.out.println("[ERROR] Please input a valid choice.");
										scan.nextLine();
									}
								}
						default: throw new IndexOutOfBoundsException();
						}
					} catch (IndexOutOfBoundsException e) {
						System.out.println("[ERROR] Please input a valid choice.");
					} catch (Exception e) {
						System.out.println("[ERROR] Please input a valid choice.");
						scan.nextLine();
					}
				}
			} else if (u.isError()) {
				System.out.println("This unit has an Error");
				System.out.println("Error: " + u.getErrorCode());
				System.out.println("--OPTIONS--");
				System.out.println("[1] Dismiss Error");
				System.out.println("[2] Change Error");
				System.out.println("[0] Return");
				int choice;
				while (true){
					try {
						System.out.print("Select Option: ");
						choice = scan.nextInt();
						scan.nextLine();
						if (choice == 0) {
							Professor.viewUnavail(room, roomSelect);
							return;
						}
						switch(choice) {
						case 2: System.out.println("Please Enter a new Error Code ");
								System.out.println("[0 to Cancel, 1 to View All Error Codes]");
								String code;
								while (true) {
									try {
										System.out.print("Error: #");
										code = scan.nextLine();
										if (code.equals("0")) {
											Professor.viewUnavail(room, roomSelect);
											return;
										} else if (code.equals("1")) {
											System.out.println(SET.errorCodes);
										} else {
											System.out.println("Issue: " + u.getErrorClaim());
											System.out.println("Code: #" + code);
											System.out.println("Confirm? [1 to Confirm, 2 to Retry, 0 to Cancel]");
											boolean retry = false;
											while (true) {
												try {
													System.out.print("Confirm: ");
													choice = scan.nextInt();
													switch(choice) {
													case 0: Professor.viewUnavail(room, roomSelect);
															return;
													case 1:	System.out.println("Confirmed.");
															int master = DATA.find(MASTERLIST, u.getRoomNo(), u.getPcNo());
															if (master == -1) System.out.println("Masterlist Error. Please try again later.");
															else {
																MASTERLIST.get(master).setDate(LocalDate.now());
																MASTERLIST.get(master).setErrorCode("#"+code);
																MASTERLIST.get(master).printUnit();
																System.out.println("[SUCCESS] Error Regenerated. Thank you!");
															}
															Professor.viewUnavail(room, roomSelect);
															return;
													case 2: retry = true;
															scan.nextLine();
															break;
													default: throw new IndexOutOfBoundsException();
													}
												} catch (IndexOutOfBoundsException e) {
													System.out.println("[ERROR] Please input a valid choice.");
												} catch (Exception e) {
													System.out.println("[ERROR] Please input a valid choice.");
													scan.nextLine();
												}
												if(retry) break;
											}
											if (retry) continue;
										}
									} catch (IndexOutOfBoundsException e) {
										System.out.println("[ERROR] Please input a valid choice.");
									} catch (Exception e) {
										System.out.println("[ERROR] Please input a valid choice.");
										scan.nextLine();
									}
								}
						case 1: System.out.println("Are you Sure? This will remove the Error.");
								System.out.println("[0 to Cancel, 1 to Confirm]");
								while (true) {
									try {
										System.out.print("Confirm: ");
										choice = scan.nextInt();
										switch(choice) {
										case 0: Professor.viewUnavail(room, roomSelect);
												return;
										case 1:	System.out.println("Confirmed.");
												int master = DATA.find(MASTERLIST, u.getRoomNo(), u.getPcNo());
												if (master == -1) System.out.println("Masterlist Error. Please try again later.");
												else {
													MASTERLIST.get(master).setDate(LocalDate.now());
													MASTERLIST.get(master).setAvailable(true);
													MASTERLIST.get(master).setError(false);
													MASTERLIST.get(master).setReported(false);
													MASTERLIST.get(master).setErrorClaim("-");
													MASTERLIST.get(master).setErrorCode("-");
													MASTERLIST.get(master).printUnit();
													System.out.println("[SUCCESS] Error Dismissed. Thank you!");
												}
												Professor.viewUnavail(room, roomSelect);
												return;
										default: throw new IndexOutOfBoundsException();
										}
									} catch (IndexOutOfBoundsException e) {
										System.out.println("[ERROR] Please input a valid choice.");
									} catch (Exception e) {
										System.out.println("[ERROR] Please input a valid choice.");
										scan.nextLine();
									}
								}
						default: throw new IndexOutOfBoundsException();
						}
					} catch (IndexOutOfBoundsException e) {
						System.out.println("[ERROR] Please input a valid choice.");
					} catch (Exception e) {
						System.out.println("[ERROR] Please input a valid choice.");
						scan.nextLine();
					}
				}
			} else if (u.isReported()) {
				System.out.println("This unit has been Reported");
				System.out.println("Issue: " + u.getErrorClaim());
				System.out.println("--OPTIONS--");
				System.out.println("[1] Generate Error");
				System.out.println("[2] Dismiss Report");
				System.out.println("[0] Return");
				int choice;
				while (true){
					try {
						System.out.println("Select Option: ");
						choice = scan.nextInt();
						scan.nextLine();
						if (choice == 0) {
							Professor.viewUnavail(room, roomSelect);
							return;
						}
						switch(choice) {
						case 1: System.out.println("Please Enter an Error Code ");
								System.out.println("[0 to Cancel, 1 to View All Error Codes]");
								String code;
								while (true) {
									try {
										System.out.print("Error: #");
										code = scan.nextLine();
										if (code.equals("0")) {
											Professor.viewUnavail(room, roomSelect);
											return;
										} else if (code.equals("1")) {
											System.out.println(SET.errorCodes);
										} else {
											System.out.println("Issue: " + u.getErrorClaim());
											System.out.println("Code: #" + code);
											System.out.println("Confirm? [1 to Confirm, 2 to Retry, 0 to Cancel]");
											boolean retry = false;
											while (true) {
												try {
													System.out.print("Confirm: ");
													choice = scan.nextInt();
													scan.nextLine();
													switch(choice) {
													case 0: Professor.viewUnavail(room, roomSelect);
															return;
													case 1:	System.out.println("Confirmed.");
															int master = DATA.find(MASTERLIST, u.getRoomNo(), u.getPcNo());
															if (master == -1) System.out.println("Masterlist Error. Please try again later.");
															else {
																MASTERLIST.get(master).setDate(LocalDate.now());
																MASTERLIST.get(master).setError(true);
																MASTERLIST.get(master).setErrorCode("#"+code);
																MASTERLIST.get(master).printUnit();
																System.out.println("[SUCCESS] Error Generated. Thank you!");
															}
															Professor.viewUnavail(room, roomSelect);
															return;
													case 2: retry = true;
															scan.nextLine();
															break;
													default: throw new IndexOutOfBoundsException();
													}
												} catch (IndexOutOfBoundsException e) {
													System.out.println("[ERROR] Please input a valid choice.");
												} catch (Exception e) {
													System.out.println("[ERROR] Please input a valid choice.");
													scan.nextLine();
												}
												if(retry) break;
											}
											if (retry) continue;
										}
									} catch (IndexOutOfBoundsException e) {
										System.out.println("[ERROR] Please input a valid choice.");
									} catch (Exception e) {
										System.out.println("[ERROR] Please input a valid choice.");
										scan.nextLine();
									}
								}
						case 2: System.out.println("Are you Sure? This will remove the Report.");
								System.out.println("[0 to Cancel, 1 to Confirm]");
								while (true) {
									try {
										System.out.print("Confirm: ");
										choice = scan.nextInt();
										scan.nextLine();
										switch(choice) {
										case 0: Professor.viewUnavail(room, roomSelect);
												return;
										case 1:	System.out.println("Confirmed.");
												int master = DATA.find(MASTERLIST, u.getRoomNo(), u.getPcNo());
												if (master == -1) System.out.println("Masterlist Error. Please try again later.");
												else {
													MASTERLIST.get(master).setDate(LocalDate.now());
													MASTERLIST.get(master).setAvailable(true);
													MASTERLIST.get(master).setReported(false);
													MASTERLIST.get(master).setErrorClaim("-");
													MASTERLIST.get(master).setErrorCode("-");
													MASTERLIST.get(master).printUnit();
													System.out.println("[SUCCESS] Report Dismissed. Thank you!");
												}
												Professor.viewUnavail(room, roomSelect);
												return;
										default: throw new IndexOutOfBoundsException();
										}
									} catch (IndexOutOfBoundsException e) {
										System.out.println("[ERROR] Please input a valid choice.");
									} catch (Exception e) {
										System.out.println("[ERROR] Please input a valid choice.");
										scan.nextLine();
									}
								}
						default: throw new IndexOutOfBoundsException();
						}
					} catch (IndexOutOfBoundsException e) {
						System.out.println("[ERROR] Please input a valid choice.");
					} catch (Exception e) {
						System.out.println("[ERROR] Please input a valid choice.");
						scan.nextLine();
					}
				}
			} else {
				System.out.println("Unit Error. Returning to Selection.");
				Professor.viewUnavail(room, roomSelect);
				return;
			}
			
		}
	}
	private class Admin extends User {
		private static final String password = "admin123";
		public static void login() {
			System.out.println("\n--ADMIN LOGIN--");
			System.out.println("Please Enter the Password [0 to Cancel]");
			while (true) {
				try  {
					System.out.print("Enter Password: ");
					String pass = scan.nextLine();
					if (pass.equals("0")) {
						User.login();
						return;
					}
					if (password.equals(pass)) {
						System.out.println("[SUCCESS] Login Sucessful. Welcome, Admin!");
						Admin.menu();
						return;
					} else {
						System.out.println("[ERROR] Invalid Username/Password. Please try again");
					}
				} catch (Exception e) {
					System.out.println("[ERROR] Please input a valid choice.");
					scan.nextLine();
				}
			}
		}
		private static void menu() {					//change to Private
			System.out.println("\n--ADMIN BOARD--");
			System.out.println("[1] Manage Reports/Errors/Repairs");
			System.out.println("[2] Display Units");
			System.out.println("[3] Add Unit / Room");
			System.out.println("[4] Remove Unit / Room");
			System.out.println("[0] Logout");
			int choice;
			while (true) {
				try {
					System.out.print("Select Option: ");
					choice = scan.nextInt();
					if (choice == 0) {
						User.login();
						return;
					} else {
						switch(choice) {
						case 1:
							Admin.manage();
							return;
						case 2:
							Admin.display(1);
							return;
						case 3:
							Admin.add();
							return;
						case 4:
							Admin.remove();
							return;
						default:
							throw new IndexOutOfBoundsException();
						}
					}
					
				} catch (IndexOutOfBoundsException e) {
					System.out.println("[ERROR] Please input a valid choice.");
				} catch (Exception e) {
					System.out.println("[ERROR] Please input a valid choice.");
					scan.nextLine();
				}
			}
		}
		private static void manage() {
			System.out.println("\n--ICTC ERRORS/REPORTS/REPAIRS--");
			ArrayList<Unit> UNAVAILABLE = SET.generateUnion(MASTERLIST, false, true, true, true);
			DATA.printIndexed(UNAVAILABLE);
			Unit select;
			int choice;
			while (true) {
				try {	
					System.out.print("View Unit [0 to Return]: ");
					choice = scan.nextInt();
					scan.nextLine();
					if (choice == 0) {
						Admin.menu();
						return;
					}
					else if (choice > 0 && choice <= UNAVAILABLE.size()) {
						System.out.print("[SELECT] ");
						select = UNAVAILABLE.get(choice-1);
						System.out.println("[SUCCESS] You have selected PC-"+select.getPcNo());
						Admin.select(select);
						return;
					} 
					else {throw new IndexOutOfBoundsException();}
				} catch (IndexOutOfBoundsException e) {
					System.out.println("[ERROR] Please input a valid choice.");
				} catch (Exception e) {
					System.out.println("[ERROR] Please input a valid choice.");
					scan.nextLine();
				}
			}
		}
		private static void select(Unit u) {
			System.out.println("\n== [SELECTED]==");
			System.out.println("[ICT" + u.getRoomNo() + " PC-" + u.getPcNo() + "]");
			if (u.isRepair()) {
				System.out.println("This unit is under Repair");
				System.out.println("--OPTIONS--");
				System.out.println("[1] Mark as Fixed");
				System.out.println("[0] Return");
				int choice;
				while (true){
					try {
						System.out.print("Select Option: ");
						choice = scan.nextInt();
						scan.nextLine();
						if (choice == 0) {
							Admin.manage();
							return;
						}
						switch(choice) {
						case 1: System.out.println("This will mark the Unit as Repaired.");
								System.out.println("[0 to Cancel, 1 to Confirm]");
								while (true) {
									try {
										System.out.print("Confirm: ");
										choice = scan.nextInt();
										switch(choice) {
										case 0: Admin.manage();
												return;
										case 1:	System.out.println("Confirmed.");
												int master = DATA.find(MASTERLIST, u.getRoomNo(), u.getPcNo());
												if (master == -1) System.out.println("Masterlist Error. Please try again later.");
												else {
													MASTERLIST.get(master).setDate(LocalDate.now());
													MASTERLIST.get(master).setAvailable(true);
													MASTERLIST.get(master).setError(false);
													MASTERLIST.get(master).setRepair(false);
													MASTERLIST.get(master).setReported(false);
													MASTERLIST.get(master).setErrorClaim("-");
													MASTERLIST.get(master).setErrorCode("-");
													MASTERLIST.get(master).printUnit();
													System.out.println("[SUCCESS] Unit is now Available. Thank you!");
												}
												Admin.manage();
												return;
										default: throw new IndexOutOfBoundsException();
										}
									} catch (IndexOutOfBoundsException e) {
										System.out.println("[ERROR] Please input a valid choice.");
									} catch (Exception e) {
										System.out.println("[ERROR] Please input a valid choice.");
										scan.nextLine();
									}
								}
						default: throw new IndexOutOfBoundsException();
						}
					} catch (IndexOutOfBoundsException e) {
						System.out.println("[ERROR] Please input a valid choice.");
					} catch (Exception e) {
						System.out.println("[ERROR] Please input a valid choice.");
						scan.nextLine();
					}
				}
			} else if (u.isError()) {
				System.out.println("This unit has an Error");
				System.out.println("Error: " + u.getErrorCode());
				System.out.println("--OPTIONS--");
				System.out.println("[1] Mark for Repair");
				System.out.println("[2] Mark as Fixed");
				System.out.println("[0] Return");
				int choice;
				while (true){
					try {
						System.out.print("Select Option: ");
						choice = scan.nextInt();
						scan.nextLine();
						if (choice == 0) {
							Admin.manage();
							return;
						}
						switch(choice) {
						case 1: System.out.println("This will mark the Unit for Repair.");
								System.out.println("[0 to Cancel, 1 to Confirm]");
								while (true) {
									try {
										System.out.print("Confirm: ");
										choice = scan.nextInt();
										switch(choice) {
										case 0: Admin.manage();
											return;
										case 1:	System.out.println("Confirmed.");
											int master = DATA.find(MASTERLIST, u.getRoomNo(), u.getPcNo());
											if (master == -1) System.out.println("Masterlist Error. Please try again later.");
											else {
												MASTERLIST.get(master).setDate(LocalDate.now());
												MASTERLIST.get(master).setRepair(true);
												System.out.println("[SUCCESS] Marked for Repair. Thank you!");
											}
											Admin.manage();
											return;
										default: throw new IndexOutOfBoundsException();
										}
									} catch (IndexOutOfBoundsException e) {
										System.out.println("[ERROR] Please input a valid choice.");
									} catch (Exception e) {
										System.out.println("[ERROR] Please input a valid choice.");
										scan.nextLine();
									}
							}
						case 2: System.out.println("This will mark the Error as Fixed");
								System.out.println("[0 to Cancel, 1 to Confirm]");
								while (true) {
									try {
										System.out.print("Confirm: ");
										choice = scan.nextInt();
										switch(choice) {
										case 0: Admin.manage();
												return;
										case 1:	System.out.println("Confirmed.");
												int master = DATA.find(MASTERLIST, u.getRoomNo(), u.getPcNo());
												if (master == -1) System.out.println("Masterlist Error. Please try again later.");
												else {
													MASTERLIST.get(master).setDate(LocalDate.now());
													MASTERLIST.get(master).setAvailable(true);
													MASTERLIST.get(master).setError(false);
													MASTERLIST.get(master).setReported(false);
													MASTERLIST.get(master).setErrorClaim("-");
													MASTERLIST.get(master).setErrorCode("-");
													MASTERLIST.get(master).printUnit();
													System.out.println("[SUCCESS] Error Dismissed. Thank you!");
												}
												Admin.manage();
												return;
										default: throw new IndexOutOfBoundsException();
										}
									} catch (IndexOutOfBoundsException e) {
										System.out.println("[ERROR] Please input a valid choice.");
									} catch (Exception e) {
										System.out.println("[ERROR] Please input a valid choice.");
										scan.nextLine();
									}
								}
						default: throw new IndexOutOfBoundsException();
						}
					} catch (IndexOutOfBoundsException e) {
						System.out.println("[ERROR] Please input a valid choice.");
					} catch (Exception e) {
						System.out.println("[ERROR] Please input a valid choice.");
						scan.nextLine();
					}
				}
			} else if (u.isReported()) {
				System.out.println("This unit has been Reported");
				System.out.println("Issue: " + u.getErrorClaim());
				System.out.println("--OPTIONS--");
				System.out.println("[1] Generate Error");
				System.out.println("[2] Dismiss Report");
				System.out.println("[0] Return");
				int choice;
				while (true){
					try {
						System.out.println("Select Option: ");
						choice = scan.nextInt();
						scan.nextLine();
						if (choice == 0) {
							Admin.manage();
							return;
						}
						switch(choice) {
						case 1: System.out.println("Please Enter an Error Code ");
								System.out.println("[0 to Cancel, 1 to View All Error Codes]");
								String code;
								while (true) {
									try {
										System.out.print("Error: #");
										code = scan.nextLine();
										if (code.equals("0")) {
											Admin.manage();
											return;
										} else if (code.equals("1")) {
											System.out.println(SET.errorCodes);
										} else {
											System.out.println("Issue: " + u.getErrorClaim());
											System.out.println("Code: #" + code);
											System.out.println("Confirm? [1 to Confirm, 2 to Retry, 0 to Cancel]");
											boolean retry = false;
											while (true) {
												try {
													System.out.print("Confirm: ");
													choice = scan.nextInt();
													scan.nextLine();
													switch(choice) {
													case 0: Admin.manage();
															return;
													case 1:	System.out.println("Confirmed.");
															int master = DATA.find(MASTERLIST, u.getRoomNo(), u.getPcNo());
															if (master == -1) System.out.println("Masterlist Error. Please try again later.");
															else {
																MASTERLIST.get(master).setDate(LocalDate.now());
																MASTERLIST.get(master).setReported(false);
																MASTERLIST.get(master).setError(true);
																MASTERLIST.get(master).setErrorCode("#"+code);
																MASTERLIST.get(master).printUnit();
																System.out.println("[SUCCESS] Error Generated. Thank you!");
															}
															Admin.manage();
															return;
													case 2: retry = true;
															scan.nextLine();
															break;
													default: throw new IndexOutOfBoundsException();
													}
												} catch (IndexOutOfBoundsException e) {
													System.out.println("[ERROR] Please input a valid choice.");
												} catch (Exception e) {
													System.out.println("[ERROR] Please input a valid choice.");
													scan.nextLine();
												}
												if(retry) break;
											}
											if (retry) continue;
										}
									} catch (IndexOutOfBoundsException e) {
										System.out.println("[ERROR] Please input a valid choice.");
									} catch (Exception e) {
										System.out.println("[ERROR] Please input a valid choice.");
										scan.nextLine();
									}
								}
						case 2: System.out.println("This will remove the Report.");
								System.out.println("[0 to Cancel, 1 to Confirm]");
								while (true) {
									try {
										System.out.print("Confirm: ");
										choice = scan.nextInt();
										scan.nextLine();
										switch(choice) {
										case 0: Admin.manage();
												return;
										case 1:	System.out.println("Confirmed.");
												int master = DATA.find(MASTERLIST, u.getRoomNo(), u.getPcNo());
												if (master == -1) System.out.println("Masterlist Error. Please try again later.");
												else {
													MASTERLIST.get(master).setDate(LocalDate.now());
													MASTERLIST.get(master).setAvailable(true);
													MASTERLIST.get(master).setReported(false);
													MASTERLIST.get(master).setErrorClaim("-");
													MASTERLIST.get(master).setErrorCode("-");
													MASTERLIST.get(master).printUnit();
													System.out.println("[SUCCESS] Report Dismissed. Thank you!");
												}
												Admin.manage();
												return;
										default: throw new IndexOutOfBoundsException();
										}
									} catch (IndexOutOfBoundsException e) {
										System.out.println("[ERROR] Please input a valid choice.");
									} catch (Exception e) {
										System.out.println("[ERROR] Please input a valid choice.");
										scan.nextLine();
									}
								}
						default: throw new IndexOutOfBoundsException();
						}
					} catch (IndexOutOfBoundsException e) {
						System.out.println("[ERROR] Please input a valid choice.");
					} catch (Exception e) {
						System.out.println("[ERROR] Please input a valid choice.");
						scan.nextLine();
					}
				}
				
			} else {
				System.out.println("[ERROR] Unit Error. Returning to Selection.");
				Admin.manage();
				return;
			}
		}
		private static void display(int display) {
			System.out.println("\n--VIEW ICTC UNITS--");
			switch(display) {
				case 1: System.out.println("Display: ALL");
						DATA.printData(MASTERLIST);		break;
				case 2:	System.out.println("Display: AVAILABLE");
						ArrayList<Unit> AVAILABLE 	=  SET.generateIntersection(MASTERLIST, true, false, false, false);
						DATA.printData( AVAILABLE); 	break;	
				case 3:	System.out.println("Display: UNAVAILABLE");
						ArrayList<Unit> UNAVAILABLE = SET.generateUnion(MASTERLIST, false, true, true, true);
						DATA.printData(	UNAVAILABLE); 	break;
			}
			System.out.println("--------------");
			System.out.println("SELECT DISPLAY");
			System.out.println("[1] All Units (Default)");
			System.out.println("[2] Available Units");
			System.out.println("[3] Unavailable Units");
			System.out.println("[0] Return");
			int choice;
			while (true) {
				try {
					System.out.print("Select Display: ");
					choice = scan.nextInt();
					if(choice==0) {
						Admin.menu();
						return;
					} else {
						switch(choice) {
						case 1:	Admin.display(choice); return;
						case 2: Admin.display(choice); return;
						case 3: Admin.display(choice); return;
						default: throw new IndexOutOfBoundsException();
						}
					}
				} catch (IndexOutOfBoundsException e) {
					System.out.println("[ERROR] Please input a valid choice.");
				} catch (Exception e) {
					System.out.println("[ERROR] Please input a valid choice.");
					scan.nextLine();
				}
			}
			
		}
		private static void add() {
			System.out.println("\n--ADD UNIT/ROOM--");
			System.out.println("[1] Add Unit");
			System.out.println("[2] Add Room");
			System.out.println("[0] Return");
			int choice;
			while (true) {
				try {
					System.out.print("Select Option: ");
					choice = scan.nextInt();
					if(choice==0) {
						Admin.menu();
						return;
					} else {
						switch(choice) {
						case 1:	Admin.addPC(); return;
						case 2: Admin.addRoom(); return;
						default: throw new IndexOutOfBoundsException();
						}
					}
				} catch (IndexOutOfBoundsException e) {
					System.out.println("[ERROR] Please input a valid choice.");
				} catch (Exception e) {
					System.out.println("[ERROR] Please input a valid choice.");
					scan.nextLine();
				}
			}
		
		
		}
		private static void addRoom() {
			int roomNo = 0, qty = 0;
			System.out.println("\n--- CREATE NEW ROOM---");
			SET.printRoomsBullet();
			System.out.println("[ENTER NEW ROOM, 0 to Cancel]");
			while (true) {
		        try {
		            System.out.print("Enter Room: ");
		            roomNo = scan.nextInt();
		            scan.nextLine(); 
		            if (roomNo == 0)  {
		            	Admin.add();
		            	return;
		            }
		            if (roomNo < 0) {
		            	System.out.println("[ERROR] Invalid input. Please enter a valid number.");
		            	continue;
		            }
		            if (ROOMS.contains(roomNo)) {
		                System.out.println("[ERROR] ICT" + roomNo + " already exists.");
		                continue;
		            }
		            System.out.println("[ENTER QUANTITY OF NEW UNITs, 0 to Return]");
		            while (true) {
		            	try {
				            System.out.print("Quantity: ");
				            qty = scan.nextInt();
				            scan.nextLine();
				            if (qty == 0) {
				            	Admin.add();
				            	return;
				            } else if (qty < 0) {
				            	System.out.println("[ERROR] Please enter a valid number.");
				            	continue;
				            } else {
				            	int addUnits = 0;
				            	final int START = 1;
				            	for (int i = 0; i < qty; i++) {
				                    int newPCNo = START + i;
				                    MASTERLIST.add(new Unit(
				                        roomNo, newPCNo, LocalDate.now(), 
				                        true, false, false, false, "-", "-"
				                    ));
				                    addUnits++;
				                }
				                ROOMS.add(roomNo); 
				                Collections.sort(MASTERLIST); 
				                System.out.println("[SUCCESS] Created ICT" + roomNo + " and added " + addUnits + " PC(s) [PC-1 to PC-" + qty + "]");
				                Admin.add();
				                return;
				            }
		            	} catch (InputMismatchException e) {
				            System.out.println("[ERROR] Please enter a number.");
				            scan.nextLine();
				        } catch (Exception e) {
				            System.out.println("[ERROR] Please try again.");
				            scan.nextLine(); 
				        }
		            }
		        } catch (InputMismatchException e) {
		            System.out.println("[ERROR] Invalid input. Please enter a number.");
		            scan.nextLine();
		        } catch (Exception e) {
		            System.out.println("[ERROR] An unexpected error occurred. Please try again.");
		            scan.nextLine(); 
		        }
		    }		
		}
		private static void addPC() {
			int roomNo = 0, pcNo = 0;
			System.out.println("\n--- ADD UNIT ---");
			SET.printRoomsBullet();
			System.out.println("[SELECT ROOM, 0 to Cancel]");
			while (true) {
		        try {
		            System.out.print("Select Room: ");
		            roomNo = scan.nextInt();
		            scan.nextLine(); 
		            if (roomNo == 0)  {
		            	Admin.add();
		            	return;
		            }
		            if (!ROOMS.contains(roomNo)) {
		                System.out.println("[ERROR] ICT" + roomNo + " does not exist. Please create Room first.");
		                continue;
		            }
		            System.out.println("[ENTER NEW UNIT, 0 to Return]");
		            while (true) {
		            	try {
				            System.out.print("New Unit: ");
				            pcNo = scan.nextInt();
				            scan.nextLine();
				            if (pcNo == 0) {
				            	Admin.add();
				            	return;
				            } else if (pcNo < 0) {
				            	System.out.println("[ERROR] Please enter a valid number.");
				            	continue;
				            } else {
				            	int index = DATA.find(MASTERLIST, roomNo, pcNo);
					            if (index != -1) {
					                System.out.println("[ERROR] PC ICT" + roomNo + " PC-" + pcNo + " already exists!");
					            } else {
					                MASTERLIST.add(new Unit(roomNo, pcNo, LocalDate.now(), true, false, false, false, "-", "-"));
					                System.out.println("[SUCCESS] PC ICT" + roomNo + " PC-" + pcNo + " added.");
					                Admin.add();
					    			Collections.sort(MASTERLIST);
					                return;
				            }
				            }
		            	} catch (InputMismatchException e) {
				            System.out.println("[ERROR] Invalid input. Please enter a number.");
				            scan.nextLine();
				        } catch (Exception e) {
				            System.out.println("[ERROR] An unexpected error occurred. Please try again.");
				            scan.nextLine(); 
				        }
		            }
		        } catch (InputMismatchException e) {
		            System.out.println("[ERROR] Invalid input. Please enter a number.");
		            scan.nextLine();
		        } catch (Exception e) {
		            System.out.println("[ERROR] An unexpected error occurred. Please try again.");
		            scan.nextLine(); 
		        }
		    }
		}
		private static void remove() {
			System.out.println("\n--REMOVE UNIT/ROOM--");
			System.out.println("[1] Remove Unit");
			System.out.println("[2] Remove Room");
			System.out.println("[0] Return");
			int choice;
			while (true) {
				try {
					System.out.print("Select Option: ");
					choice = scan.nextInt();
					if(choice==0) {
						Admin.menu();
						return;
					} else {
						switch(choice) {
						case 1:	Admin.removePC(); return;
						case 2: Admin.removeRoom(); return;
						default: throw new IndexOutOfBoundsException();
						}
					}
				} catch (IndexOutOfBoundsException e) {
					System.out.println("Please input a valid choice.");
				} catch (Exception e) {
					System.out.println("Please input a valid choice.");
					scan.nextLine();
				}
			}
		}
		private static void removeRoom() {
			System.out.println("\n--- REMOVE ROOM ---");
			SET.printRoomsBullet();
			System.out.println("[SELECT ROOM, 0 to Cancel]");
			while (true) {
		        try {
		        	int delete;
		            System.out.print("Select Room: ");
		            delete = scan.nextInt();
		            scan.nextLine(); 
		            if (delete == 0)  {
		            	Admin.remove();
		            	return;
		            }
		            if (!ROOMS.contains(delete)) {
		                System.out.println("[ERROR] ICT" + delete + " does not exist.");
		                continue;
		            } else {
		            	long count = SET.generateRoom(MASTERLIST, delete).stream().count();
		            	System.out.println("\n!!! WARNING !!!");
		                System.out.println("This will delete ALL PCs ("+ count +" unit/s) in Room ICT" + delete + ".");
		                System.out.print("Confirm deletion? Type 'YES' to proceed: ");
		                String confirmation = scan.nextLine();
		                if (confirmation.trim().toUpperCase().equals("YES")) {
		                    MASTERLIST.removeIf(u -> u.getRoomNo() == delete);
		                    ROOMS.remove(delete); 
		                    System.out.println("[SUCCESS] Room ICT" + delete + " have been permanently removed.");
		                    Admin.remove();
		                    return;
		                } else {
		                    System.out.println("[CANCELLED] Deletion cancelled.");
		                    Admin.remove();
		                    return;
		                }
		            }
		            
		        } catch (InputMismatchException e) {
		            System.out.println("[ERROR] Invalid input. Please enter a number.");
		            scan.nextLine();
		        } catch (Exception e) {
		            System.out.println("[ERROR] An unexpected error occurred. Please try again.");
		            scan.nextLine(); 
		        }
			}
		}
		private static void removePC() {
			System.out.println("\n--- REMOVE UNIT ---");
			SET.printRoomsBullet();
			int pcDelete = 0, roomDelete = 0;
			System.out.println("[SELECT ROOM, 0 to Cancel]");
			while (true) {
		        try {
		            System.out.print("Select Room: ");
		            roomDelete = scan.nextInt();
		            scan.nextLine(); 
		            if (roomDelete == 0)  {
		            	Admin.remove();
		            	return;
		            }
		            if (!ROOMS.contains(roomDelete)) {
		                System.out.println("[ERROR] ICT" + roomDelete + " does not exist.");
		                continue;
		            } else {
		            	System.out.println("[SELECT UNIT, 0 to Cancel]");
		            	while (true) {
		                    try {
		                    	System.out.print("Select Unit: ");
		                    	pcDelete = scan.nextInt();
		                        scan.nextLine(); 
		                        if (pcDelete == 0) {
		                        	Admin.remove();
		    		            	return;
		                        }
		                       
		                        int index = DATA.find(MASTERLIST, roomDelete, pcDelete);
		                        if (index == -1) {
		                            System.out.println("[ERROR] PC-" + pcDelete + " does not exist in ICT" + roomDelete + ".");
		                            continue; 
		                        }
		                        
		                        System.out.println("\n!!! WARNING !!!");
		                        System.out.println("You are about to delete PC-" + pcDelete + " in ICT" + roomDelete + ".");
		      
		                        MASTERLIST.get(index).printUnit(); 
		                        System.out.print("Confirm deletion? Type 'YES' to proceed: ");
		                        String confirmation = scan.nextLine();

		                        if (confirmation.trim().toUpperCase().equals("YES")) {
		                            int deleteKey = roomDelete;
		                            MASTERLIST.remove(index);
		     
		                            long remainingPCs = MASTERLIST.stream().filter(u -> u.getRoomNo() == deleteKey).count();
		                        
		                            if (remainingPCs == 0) {
		                                LBJ_Patrol.ROOMS.remove(roomDelete);
		                                System.out.println("[SUCCESS] PC-" + pcDelete + " removed. ICT" + roomDelete + " has been removed from the room list.");
		                            } else {
		                                System.out.println("[SUCCESS] PC-" + pcDelete + " in ICT" + roomDelete + " has been permanently removed.");
		                            }
		                            Admin.remove();
		                            return;
		                        } else {
		                            System.out.println("[CANCELLED] Deletion cancelled.");
		                            Admin.remove();
		                            return;
		                        }

		                    } catch (InputMismatchException e) {
		                        System.out.println("[ERROR] Invalid input. Please enter a number.");
		                        scan.nextLine();
		                    } catch (Exception e) {
		                        System.out.println("[ERROR] An unexpected error occurred. Please try again.");
		                        scan.nextLine();
		                    }
		                }
		            }
		            
		        } catch (InputMismatchException e) {
		            System.out.println("[ERROR] Invalid input. Please enter a number.");
		            scan.nextLine();
		        } catch (Exception e) {
		            System.out.println("[ERROR] An unexpected error occurred. Please try again.");
		            scan.nextLine(); 
		        }
			}
		}
	}
	//PROPOSITIONAL STATEMENT/FUNCTION
	public static class Unit implements Comparable<Unit> {
		private int roomNo, pcNo;
	    private boolean available, reported, error, repair;
	    private LocalDate date;
	    private String errorCode, errorClaim;

	    public Unit(
	    		int roomNo, 
	    		int pcNo, 
	    		LocalDate date,
	    		boolean available, 
	    		boolean reported, 
	    		boolean error, 
	    		boolean repair,
	    		String errorCode, 
	    		String errorClaim) 
	    {
	        
	        this.roomNo = roomNo;
	        this.pcNo = pcNo;
	        this.date = date;

	        this.available = available;
	        this.reported = reported;
	        this.error = error;
	        this.repair = repair;

	        this.errorCode = errorCode;
	        this.errorClaim = errorClaim;
	    }

	    public int getRoomNo()							{return roomNo;}
	    public void setRoomNo(int roomNo)				{this.roomNo = roomNo;}
	    
	    public int getPcNo() 							{return pcNo;}
	    public void setPcNo(int pcNo) 					{this.pcNo = pcNo;}

	    public LocalDate getDate() 						{return date;}
	    public void setDate(LocalDate date) 			{this.date = date; }

	    public boolean isAvailable()	 				{return available;}
	    public void setAvailable(boolean available) 	{this.available = available;}

	    public boolean isReported() 					{return reported;}
	    public void setReported(boolean reported) 		{this.reported = reported;}

	    public boolean isError() 						{return error;}

	    public void setError(boolean error) 			{this.error = error;}

	    public boolean isRepair() 						{return repair;}
	    public void setRepair(boolean repair) 			{this.repair = repair;}

	    public String getErrorCode() 					{return errorCode;}

	    public void setErrorCode(String errorCode)		{this.errorCode = errorCode;}

	    public String getErrorClaim() 					{return errorClaim;}

	    public void setErrorClaim(String errorClaim) 	{this.errorClaim = errorClaim;}

		public void printUnit() {
			String status = this.isAvailable() ? "Available" : "Unavailable";
            if (this.isReported()) status = "Reported";
            if (this.isError()) status = getErrorCode();
            if (this.isRepair()) status = "Repairing";
			System.out.println(String.format("ICT%-10d PC-%-10d %-15s As of %-15s %-20s", 
                    				this.getRoomNo(),
                    				this.getPcNo(), status, 
                    				this.getDate().format(DATE_FMT),
                    				this.getErrorClaim()));
		}
		
	    @Override
        public String toString() {
            return 	"ICT-" + roomNo + 
            		" PC-" + pcNo  + "\t" +
                   (available ? "Available" : "Unavailable") + "\t" +
                   (reported ? " [REPORTED]" : "") +
                   (error ? " [ERROR: " + errorCode + "]" : "") +
                   (repair ? " [REPAIRING]" : "") +
                   (errorClaim != "" ?" Claim: " + errorClaim : "")
                   + " [As of " + date + "] "
                   ;
        }
	    @Override
	    public int compareTo(Unit other) {
	        // 1. Sort by Room Number first
	        if (this.roomNo != other.roomNo) {
	            return Integer.compare(this.roomNo, other.roomNo);
	        }
	        // 2. If Room Numbers are the same, sort by PC Number
	        return Integer.compare(this.pcNo, other.pcNo);
	    }
	    public String toDB() {
	    	return 	roomNo + "," + 
	    			pcNo + "," + 
	    			date.format(DATE_FMT) + "," + 
	    			available + "," + 
	    			reported + "," + 
	    			error + "," + 
	    			repair + "," + 
	    			errorCode + "," + 
	    			errorClaim;
	    }
	    
	}
	public static class MainGUI extends JFrame {
		private static final long serialVersionUID = 1L;
		private CardLayout cardLayout;
        private JPanel mainPanel;
        
        private JComboBox<Integer> studentRoomCombo, profRoomCombo;
        private JTable studentTable, profAvailTable, profIssueTable, adminTable;
        private DefaultTableModel studentModel, profAvailModel, profIssueModel, adminModel;
        
        private int currentRoomSelection = -1;

        public MainGUI() {
            setTitle("LBJ Unit Patrol: Your Go-To Lab Buddy");
            setSize(1100, 700);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    try { DATA.saveData(); } catch (IOException ex) { ex.printStackTrace(); }
                }
            });

            cardLayout = new CardLayout();
            mainPanel = new JPanel(cardLayout);

            initLoginScreen();
            
            initStudentMenu();
            initStudentView();
            
            initProfLogin();
            initProfMenu();
            initProfTables();
            
            
            initAdminLogin();
            initAdminDashboard();

            add(mainPanel);
            setExtendedState(JFrame.NORMAL);
            setState(JFrame.NORMAL);
            setVisible(true); 
        }

        private void nav(String cardName) {
            cardLayout.show(mainPanel, cardName);
        }

        private void initLoginScreen() {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            
            JLabel title = new JLabel("LBJ UNIT PATROL");
            JLabel title2 = new JLabel("YOUR GO-TO LAB BUDDY");
            JLabel subtitle = new JLabel("By LBJ Systems");
            JLabel body = new JLabel("By Leanne Fajardo, Benjamin Teodoro, and Jian Jimenez");
            
            title.setFont(new Font("Verdana", Font.BOLD, 60));
            title2.setFont(new Font("Verdana", Font.BOLD, 40));
            subtitle.setFont(new Font("Verdana", Font.BOLD, 20));
            body.setFont(new Font("Verdana", Font.BOLD, 20));
            
            JButton btnStudent = createStyledButton("STUDENT PORTAL", Color.decode("#3498db"));
            JButton btnProf = createStyledButton("PROFESSOR PORTAL", Color.decode("#e67e22"));
            JButton btnAdmin = createStyledButton("ADMIN DASHBOARD", Color.decode("#e74c3c"));

            btnStudent.addActionListener(e -> { refreshRoomCombos(); nav("STUDENT_MENU"); });
            btnProf.addActionListener(e -> nav("PROF_LOGIN"));
            btnAdmin.addActionListener(e -> nav("ADMIN_LOGIN"));

            gbc.gridx = 0; gbc.gridy = 0; panel.add(title, gbc);
            gbc.gridy = 1; panel.add(title2, gbc);
            gbc.gridy = 2; panel.add(subtitle, gbc);
            gbc.gridy = 3; panel.add(body, gbc);
            gbc.gridy = 4; panel.add(btnStudent, gbc);
            gbc.gridy = 5; panel.add(btnProf, gbc);
            gbc.gridy = 6; panel.add(btnAdmin, gbc);

            mainPanel.add(panel, "LOGIN");
        }

        //STUDENT
        private void initStudentMenu() {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);

            JLabel lbl = new JLabel("Select ICTC Room:");
            lbl.setFont(new Font("Verdana", Font.BOLD, 40));
            studentRoomCombo = new JComboBox<>();
            studentRoomCombo.setPreferredSize(new Dimension(150, 40)); 
            studentRoomCombo.setFont(new Font("Verdana", Font.BOLD, 20));
            
            JButton btnGo = new JButton("Enter Room");
            btnGo.setPreferredSize(new Dimension(150, 40));
            btnGo.setFont(new Font("Verdana", Font.BOLD, 15));
            
            btnGo.addActionListener(e -> {
                if(studentRoomCombo.getSelectedItem() == null) return;
                currentRoomSelection = (int) studentRoomCombo.getSelectedItem();
                refreshStudentTable();
                nav("STUDENT_VIEW");
            });

            JButton btnBack = new JButton("Back");
            btnBack.setPreferredSize(new Dimension(150, 40));
            btnBack.setFont(new Font("Verdana", Font.BOLD, 20));
            btnBack.addActionListener(e -> nav("LOGIN"));

            gbc.gridx = 0; gbc.gridy = 0; panel.add(lbl, gbc);
            gbc.gridy = 1; panel.add(studentRoomCombo, gbc);
            gbc.gridy = 2; panel.add(btnGo, gbc);
            gbc.gridy = 3; panel.add(btnBack, gbc);

            mainPanel.add(panel, "STUDENT_MENU");
        }

        private void initStudentView() {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Header
            JLabel header = new JLabel("Room View", SwingConstants.CENTER);
            header.setFont(new Font("Verdana", Font.BOLD, 30));
            panel.add(header, BorderLayout.NORTH);

            // Table
            String[] cols = {"Room No.", "PC No.", "Status", "Issue / Claim", "Date"};
            studentModel = new DefaultTableModel(cols, 0) {
            	@Override
                public boolean isCellEditable(int row, int column) { return false; }
            };
            
            studentTable = new JTable(studentModel);
            studentTable.setRowHeight(40);
            studentTable.setFont(new Font("Verdana", Font.PLAIN, 20));
            
            JTableHeader tableHeader = studentTable.getTableHeader();
            tableHeader.setFont(new Font("Verdana", Font.BOLD, 20));
            
            JScrollPane scroll = new JScrollPane(studentTable);
            panel.add(scroll, BorderLayout.CENTER);

            // Controls
            JPanel bottomPanel = new JPanel(new FlowLayout());
            JButton btnReport = new JButton("Report Selected Unit");
            btnReport.setBackground(Color.RED);
            btnReport.setForeground(Color.RED);
            btnReport.setPreferredSize(new Dimension(300, 50));
            btnReport.setFont(new Font("Verdana", Font.BOLD, 15));
            
            btnReport.addActionListener(e -> {
                int row = studentTable.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(this, "Please select a Unit from the table first.");
                    return;
                }
                int pcNo = (int) studentModel.getValueAt(row, 1);
                Unit u = findUnit(currentRoomSelection, pcNo);
                
                if (u.isRepair()) { JOptionPane.showMessageDialog(this, "Unit is under repair."); return; }
                if (u.isError()) { JOptionPane.showMessageDialog(this, "Unit has an Error Already"); return; }
                if (u.isReported()) { JOptionPane.showMessageDialog(this, "Unit is already reported."); return; }
                
                String claim = JOptionPane.showInputDialog(this, "Describe the issue for PC-" + pcNo + ":");
                if (claim != null && !claim.trim().isEmpty()) {
                    u.setReported(true);
                    u.setErrorClaim(claim);
                    u.setDate(LocalDate.now());
                    saveAndRefresh();
                    JOptionPane.showMessageDialog(this, "Report Submitted!");
                }
            });

            JButton btnBack = new JButton("Return to Menu");
            btnBack.addActionListener(e -> nav("STUDENT_MENU"));
            btnBack.setPreferredSize(new Dimension(300, 50));
            btnBack.setFont(new Font("Verdana", Font.BOLD, 15));

            bottomPanel.add(btnReport);
            bottomPanel.add(btnBack);
            panel.add(bottomPanel, BorderLayout.SOUTH);

            mainPanel.add(panel, "STUDENT_VIEW");
        }
        
        //PROFESSOR
        private void initProfLogin() {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);

            JTextField userTxt = new JTextField(15);
            JPasswordField passTxt = new JPasswordField(15);
            JLabel header = new JLabel("Professor Login");
            JLabel label1 = new JLabel("Username:");
            JLabel label2 = new JLabel("Password:");
            JButton btnLogin = new JButton("Login");
            JButton btnBack = new JButton("Back");
            
            header.setFont(new Font("Verdana", Font.BOLD, 40));
            label1.setFont(new Font("Verdana", Font.BOLD, 15));
            label2.setFont(new Font("Verdana", Font.BOLD, 15));
            
            userTxt.setPreferredSize(new Dimension(200, 30));
            userTxt.setFont(new Font("Verdana", Font.BOLD, 15));
            passTxt.setPreferredSize(new Dimension(200, 30));
            passTxt.setFont(new Font("Verdana", Font.BOLD, 15));
            
            btnLogin.setPreferredSize(new Dimension(200, 50));
            btnLogin.setFont(new Font("Verdana", Font.BOLD, 15));
            
            btnBack.setPreferredSize(new Dimension(200, 50));
            btnBack.setFont(new Font("Verdana", Font.BOLD, 15));

            btnLogin.addActionListener(e -> {
                String u = userTxt.getText();
                String p = new String(passTxt.getPassword());
                Map<String, String> creds = new HashMap<>();
                creds.put("brteodoro", "benjamin"); creds.put("lvfajardo", "leanne"); 
                creds.put("jtjimenez", "jian"); creds.put("a", "a");

                if (creds.containsKey(u) && creds.get(u).equals(p)) {
                    userTxt.setText(""); passTxt.setText("");
                    refreshRoomCombos();
                    nav("PROF_MENU");
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Credentials");
                }
            });

            btnBack.addActionListener(e -> nav("LOGIN"));
            
            gbc.gridx = 0; gbc.gridy = 0; panel.add(header, gbc);
            gbc.gridy = 1; panel.add(label1, gbc);
            gbc.gridy = 2; panel.add(userTxt, gbc);
            gbc.gridy = 3; panel.add(label2, gbc);
            gbc.gridy = 4; panel.add(passTxt, gbc);
            gbc.gridy = 5; panel.add(btnLogin, gbc);
            gbc.gridy = 6; panel.add(btnBack, gbc);

            mainPanel.add(panel, "PROF_LOGIN");
        }

        private void initProfMenu() {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            JLabel lbl = new JLabel("Select Room:");
            lbl.setFont(new Font("Verdana", Font.BOLD, 40));
            profRoomCombo = new JComboBox<>();
            profRoomCombo.setPreferredSize(new Dimension(150, 40)); 
            profRoomCombo.setFont(new Font("Verdana", Font.BOLD, 20));

            JButton btnView = new JButton("View Room");
            btnView.setPreferredSize(new Dimension(150, 40)); 
            btnView.setFont(new Font("Verdana", Font.BOLD, 15));
            
            btnView.addActionListener(e -> {
                if(profRoomCombo.getSelectedItem() == null) return;
                currentRoomSelection = (int) profRoomCombo.getSelectedItem();
                refreshProfTables();
                nav("PROF_TABLES");
            });

            JButton btnLogout = new JButton("Logout");
            btnLogout.addActionListener(e -> nav("LOGIN"));
            btnLogout.setPreferredSize(new Dimension(150, 40)); 
            btnLogout.setFont(new Font("Verdana", Font.BOLD, 20));

            gbc.gridx = 0; gbc.gridy = 0; panel.add(lbl, gbc);
            gbc.gridy = 1; panel.add(profRoomCombo, gbc);
            gbc.gridy = 2; panel.add(btnView, gbc);
            gbc.gridy = 3; panel.add(btnLogout, gbc);

            mainPanel.add(panel, "PROF_MENU");
        }

        private void initProfTables() {
            JTabbedPane tabs = new JTabbedPane();
            tabs.setPreferredSize(new Dimension(500, 30));
            tabs.setFont(new Font("Verdana", Font.BOLD, 30));

            JPanel pnlAvail = new JPanel(new BorderLayout());
            profAvailModel = new DefaultTableModel(new String[]{"Room No.", "PC No.", "Status", "As of Date"}, 0);
            profAvailTable = new JTable(profAvailModel);
            
            profAvailTable.setRowHeight(40);
            profAvailTable.setFont(new Font("Verdana", Font.PLAIN, 20));
            JTableHeader tableHeader1 = profAvailTable.getTableHeader();
            tableHeader1.setFont(new Font("Verdana", Font.BOLD, 20));
            
            pnlAvail.add(new JScrollPane(profAvailTable), BorderLayout.CENTER);
            tabs.addTab("Available Units", pnlAvail);
            
            JPanel pnlIssues = new JPanel(new BorderLayout());
            profIssueModel = new DefaultTableModel(new String[]{"Room No.", "PC No.", "Status", "Issue/Code", "As of Date"}, 0);
            profIssueTable = new JTable(profIssueModel);
            
            profIssueTable.setRowHeight(40);
            profIssueTable.setFont(new Font("Verdana", Font.PLAIN, 20));
            JTableHeader tableHeader2 = profIssueTable.getTableHeader();
            tableHeader2.setFont(new Font("Verdana", Font.BOLD, 20));
            
            pnlIssues.add(new JScrollPane(profIssueTable), BorderLayout.CENTER);
            
            JPanel issueControls = new JPanel();
            JButton btnAction = new JButton("Manage Selected Unit");
            btnAction.setFont(new Font("Verdana", Font.BOLD, 30));
            btnAction.setPreferredSize(new Dimension(500, 100));
            btnAction.setForeground(Color.BLUE);
            btnAction.setBackground(Color.BLUE);
            
            btnAction.addActionListener(e -> handleProfAction());
            issueControls.add(btnAction);
            pnlIssues.add(issueControls, BorderLayout.SOUTH);
            
            tabs.addTab("Issues & Reports", pnlIssues);
            
            tabs.setForegroundAt(0, Color.BLUE);
            tabs.setForegroundAt(1, Color.RED);


            JPanel container = new JPanel(new BorderLayout());
            container.add(tabs, BorderLayout.CENTER);
            
            JButton btnBack = new JButton("Back to Room Select");
            btnBack.setFont(new Font("Verdana", Font.BOLD, 30));
            btnBack.setPreferredSize(new Dimension(250, 50));
            
            btnBack.addActionListener(e -> nav("PROF_MENU"));
            container.add(btnBack, BorderLayout.SOUTH);

            mainPanel.add(container, "PROF_TABLES");
        }

        private void handleProfAction() {
            int row = profIssueTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a Unit from the table first.");
                return;
            };
            
            int pcNo = (int) profIssueTable.getValueAt(row, 1);
            Unit u = findUnit(currentRoomSelection, pcNo);
            
            Object[] options = {};
            int choice = -1;

            if (u.isRepair()) {
                options = new Object[]{"Mark as Fixed", "Cancel"};
                choice = JOptionPane.showOptionDialog(this, "Unit is under repair.", "Manage Unit",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
                if (choice == 0) {
                	u.setReported(false); u.setError(false); u.setRepair(false); u.setAvailable(true); u.setErrorClaim("-"); u.setErrorCode("-");
                }
            } else if (u.isError()) {
                options = new Object[]{"Dismiss Error", "Edit Code", "Cancel"};
                choice = JOptionPane.showOptionDialog(this, "Current Error: " + u.getErrorCode(), "Manage Unit",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                
                if (choice == 0) {
                	u.setReported(false); u.setError(false); u.setAvailable(true); u.setErrorCode("-"); u.setErrorClaim("-");
                } else if (choice == 1) {
                    String code = JOptionPane.showInputDialog(this, "Enter New Code:", u.getErrorCode());
                    if(code != null) u.setErrorCode(code);
                }
            } else if (u.isReported()) {
                options = new Object[]{"Generate Error Code", "Dismiss Report", "Cancel"};
                choice = JOptionPane.showOptionDialog(this, "Review Report: " + u.getErrorClaim(), "Manage Unit",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                
                if (choice == 0) { // Generate Error
                     String code = JOptionPane.showInputDialog(this, SET.errorCodes + "\nEnter Code (e.g. #NoNet):");
                     if(code != null) {
                         u.setError(true); u.setAvailable(false); u.setErrorCode(code);
                     }
                } else if (choice == 1) { // Dismiss
                    u.setReported(false); u.setAvailable(true); u.setErrorClaim("-");
                }
            } 
            if (choice != -1 && choice != 2) saveAndRefresh();
        }
        
        //ADMIN
        private void initAdminLogin() {
            JPanel panel = new JPanel(new GridBagLayout());
            JPasswordField passTxt = new JPasswordField(15);
            JButton btnLogin = new JButton("Login");
            JButton btnBack = new JButton("Back");
            JLabel head = new JLabel("Administrator Login");
            JLabel label = new JLabel("Admin Password:");
            
            head.setFont(new Font("Verdana", Font.BOLD, 40));
            label.setFont(new Font("Verdana", Font.BOLD, 15));
            
            passTxt.setPreferredSize(new Dimension(200, 30));
            passTxt.setFont(new Font("Verdana", Font.BOLD, 15));
            
            btnLogin.setPreferredSize(new Dimension(200, 50));
            btnLogin.setFont(new Font("Verdana", Font.BOLD, 15));
            btnBack.setPreferredSize(new Dimension(200, 50));
            btnBack.setFont(new Font("Verdana", Font.BOLD, 15));

            btnLogin.addActionListener(e -> {
                if (new String(passTxt.getPassword()).equals("admin123")) {
                    passTxt.setText("");
                    refreshAdminTable();
                    nav("ADMIN_DASH");
                } else {
                    JOptionPane.showMessageDialog(this, "Wrong Password");
                }
            });
            btnBack.addActionListener(e -> nav("LOGIN"));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5,5,5,5);
            gbc.gridy=0; panel.add(head, gbc);
            gbc.gridy=1; panel.add(label, gbc);
            gbc.gridy=2; panel.add(passTxt, gbc);
            gbc.gridy=3; panel.add(btnLogin, gbc);
            gbc.gridy=4; panel.add(btnBack, gbc);

            mainPanel.add(panel, "ADMIN_LOGIN");
        }
        private void initAdminDashboard() {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

            // ToolBar
            JToolBar tools = new JToolBar();
            JButton btnAddRoom = new JButton("Add Room");
            JButton btnAddUnit = new JButton("Add Unit");
            JButton btnRemove = new JButton("Remove Selected");
            JButton btnRemoveRoom = new JButton("Remove Room");
            JButton btnRepair = new JButton("Mark Repair");
            JButton btnFix = new JButton("Mark Fixed");
            JButton btnLogout = new JButton("Logout");
            
            btnAddRoom.setForeground(Color.WHITE);
            btnAddUnit.setForeground(Color.WHITE);
            btnRemove.setForeground(Color.WHITE);
            btnRemoveRoom.setForeground(Color.WHITE);
            btnRepair.setForeground(Color.WHITE);
            btnFix.setForeground(Color.WHITE);
            btnLogout.setForeground(Color.WHITE);
            
            btnAddRoom.setBackground(Color.BLUE);
            btnAddUnit.setBackground(Color.BLUE);
            btnRemove.setBackground(Color.RED);
            btnRemoveRoom.setBackground(Color.RED);
            btnRepair.setBackground(new Color(138, 136, 49));
            btnFix.setBackground(new Color(55, 138, 49));
            btnLogout.setBackground(Color.BLUE);
            
            btnAddRoom.setFont(new Font("Verdana", Font.BOLD, 15));
            btnAddUnit.setFont(new Font("Verdana", Font.BOLD, 15));
            btnRemove.setFont(new Font("Verdana", Font.BOLD, 15));
            btnRemoveRoom.setFont(new Font("Verdana", Font.BOLD, 15));
            btnRepair.setFont(new Font("Verdana", Font.BOLD, 15));
            btnFix.setFont(new Font("Verdana", Font.BOLD, 15));
            btnLogout.setFont(new Font("Verdana", Font.BOLD, 15));
           
           

            btnAddRoom.addActionListener(e -> adminAddRoom());
            btnAddUnit.addActionListener(e -> adminAddUnit());
            btnRemove.addActionListener(e -> adminRemove());
            btnRemoveRoom.addActionListener(e -> adminRemoveRoom());
            btnRepair.addActionListener(e -> adminSetStatus(true));
            btnFix.addActionListener(e -> adminSetStatus(false));
            btnLogout.addActionListener(e -> nav("LOGIN"));

            tools.add(btnAddRoom); tools.add(btnAddUnit); tools.addSeparator();
            tools.add(btnRemove); tools.add(btnRemoveRoom); tools.addSeparator();
            tools.add(btnRepair); 
            tools.addSeparator();
            tools.add(btnFix); tools.add(Box.createHorizontalGlue());
            tools.add(btnLogout);

            // Table
            adminModel = new DefaultTableModel(new String[]{"Room", "PC", "Status", "Code/Issue", "Date"}, 0);
            adminTable = new JTable(adminModel);
            adminTable.setAutoCreateRowSorter(true);
            
            adminTable.setRowHeight(40);
            adminTable.setFont(new Font("Verdana", Font.PLAIN, 20));
            JTableHeader tableHeader = adminTable.getTableHeader();
            tableHeader.setFont(new Font("Verdana", Font.BOLD, 20));
            

            panel.add(tools, BorderLayout.NORTH);
            panel.add(new JScrollPane(adminTable), BorderLayout.CENTER);

            mainPanel.add(panel, "ADMIN_DASH");
        }

        private void adminAddRoom() {
            String input = JOptionPane.showInputDialog("Enter New Room Number:");
            if(input == null) return;
            try {
                int r = Integer.parseInt(input);
                if(ROOMS.contains(r)) { JOptionPane.showMessageDialog(this, "Exists!"); return; }
                String qStr = JOptionPane.showInputDialog("How many PCs?");
                int q = Integer.parseInt(qStr);
                for(int i=1; i<=q; i++) {
                    MASTERLIST.add(new Unit(r, i, LocalDate.now(), true, false, false, false, "-", "-"));
                }
                ROOMS.add(r);
                saveAndRefresh();
            } catch(Exception e) { JOptionPane.showMessageDialog(this, "Invalid Number"); }
        }

        private void adminAddUnit() {
             String rStr = JOptionPane.showInputDialog("Room Number:");
             if (rStr!=null) {
             try {
            		 int r = Integer.parseInt(rStr);
                     if(!ROOMS.contains(r)) { JOptionPane.showMessageDialog(this, "Room doesn't exist"); return; }
                     String pStr = JOptionPane.showInputDialog("PC Number:");
                     if (pStr!=null) {
                     try {
                    		 int p = Integer.parseInt(pStr);
                             if(findUnit(r, p) != null) { JOptionPane.showMessageDialog(this, "PC exists"); return; }
                             MASTERLIST.add(new Unit(r, p, LocalDate.now(), true, false, false, false, "-", "-"));
                             saveAndRefresh();
                     } catch(Exception e) { JOptionPane.showMessageDialog(this, "Invalid Input"); }}
             } catch(Exception e) { JOptionPane.showMessageDialog(this, "Invalid Input"); }}
        }

        private void adminRemove() {
            int row = adminTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a Unit from the table first.");
                return;
            };
            int r = (int) adminTable.getValueAt(row, 0);
            int p = (int) adminTable.getValueAt(row, 1);
            
            Unit u = findUnit(r, p);
            Object[] options = {};
            int choice = -1;
            options = new Object[]{"Confirm", "Cancel"};
            choice = JOptionPane.showOptionDialog(this, "This will permanently remove this Unit.", "Remove Unit",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
            if (choice == 0) {
            	MASTERLIST.remove(u);
            }
            
            long remaining = MASTERLIST.stream().filter(un -> un.getRoomNo() == r).count();
            if(remaining == 0) ROOMS.remove(r);
            
            saveAndRefresh();
        }
        private void adminRemoveRoom() {
        	String rStr = JOptionPane.showInputDialog("Room Number:");
            if (rStr!=null) {
            try {
           		 int r = Integer.parseInt(rStr);
                    if(!ROOMS.contains(r)) { JOptionPane.showMessageDialog(this, "Room doesn't exist"); return; }
                    	Object[] options = {};
                        int choice = -1;
                        options = new Object[]{"Confirm", "Cancel"};
                        choice = JOptionPane.showOptionDialog(this, "This will permanently remove this Room.", "Remove Room",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                        if (choice == 0) {
                        	for (Unit u : SET.generateRoom(MASTERLIST, r)) {
                        		MASTERLIST.remove(u);
                        	}
                        	ROOMS.remove(r);
                        }
            } catch(Exception e) { JOptionPane.showMessageDialog(this, "Invalid Input"); }}
            saveAndRefresh();
        }

        private void adminSetStatus(boolean toRepair) {
            int row = adminTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a Unit from the table first.");
                return;
            };
            int r = (int) adminTable.getValueAt(row, 0);
            int p = (int) adminTable.getValueAt(row, 1);
            Unit u = findUnit(r, p);
            
            Object[] options = {};
            int choice = -1;

            if (u.isAvailable() && !u.isReported()) {
            	options = new Object[]{"Exit"};
                choice = JOptionPane.showOptionDialog(this, "This Unit is already available", "Manage Unit",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
            }
            else if (toRepair) {
            	if (u.isRepair()) {
                	options = new Object[]{"Exit"};
                    choice = JOptionPane.showOptionDialog(this, "This Unit is already under repair.", "Manage Unit",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                } else {
                	options = new Object[]{"Repair", "Cancel"};
                    choice = JOptionPane.showOptionDialog(this, "Mark the Unit for Repair?", "Manage Unit",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
                    if (choice == 0) {
                    	u.setRepair(true); u.setAvailable(false);
                    }
                }
            } else {
            	options = new Object[]{"Fix", "Cancel"};
                choice = JOptionPane.showOptionDialog(this, "Mark the Unit as Fixed?", "Manage Unit",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
                if (choice == 0) {
                	u.setRepair(false); u.setAvailable(true); u.setError(false); u.setReported(false);
                    u.setErrorClaim("-"); u.setErrorCode("-");
                }
            }
            saveAndRefresh();
        }
        
        private JButton createStyledButton(String text, Color bg) {
            JButton b = new JButton(text);
            b.setFont(new Font("Verdana", Font.BOLD, 16));
            b.setBackground(bg);
            b.setForeground(Color.BLUE);
            b.setPreferredSize(new Dimension(250, 100));
            b.setFocusPainted(false);
            return b;
        }

        private void refreshRoomCombos() {
            studentRoomCombo.removeAllItems();
            profRoomCombo.removeAllItems();
            ArrayList<Integer> sortedRooms = new ArrayList<>(ROOMS);
            Collections.sort(sortedRooms);
            for(int r : sortedRooms) {
                studentRoomCombo.addItem(r);
                profRoomCombo.addItem(r);
            }
        }

        //SET THEORY
        private void refreshStudentTable() {
            studentModel.setRowCount(0);
            ArrayList<Unit> roomUnits = SET.generateRoom(MASTERLIST, currentRoomSelection);
            for(Unit u : roomUnits) {
                String status = "Available";
                String issue = "-";
                if(u.isRepair()) { status = "Repairing"; issue = "Under Maintenance"; }
                else if(u.isError()) { status = "Error"; issue = u.getErrorCode(); }
                else if(u.isReported()) { status = "Reported"; issue = u.getErrorClaim(); }
                
                studentModel.addRow(new Object[]{"ICT"+u.getRoomNo(),u.getPcNo(), status, issue, u.getDate()});
            }
        }

        private void refreshProfTables() {
            profAvailModel.setRowCount(0);
            profIssueModel.setRowCount(0);
            ArrayList<Unit> roomUnits = SET.generateRoom(MASTERLIST, currentRoomSelection);
            ArrayList<Unit> UNAVAILABLE = SET.generateUnion(roomUnits, false, true, true, true);
            ArrayList<Unit> AVAILABLE = SET.generateIntersection(roomUnits, true, false, false, false);
            for(Unit p: UNAVAILABLE ){
                String status = "Unknown";
                String info = "";
                if(p.isReported()) { status = "Reported"; info = p.getErrorClaim(); }
                if(p.isError()) { status = "Error"; info = p.getErrorCode(); }
                if(p.isRepair()) { status = "Repairing"; info = "Maintenance"; }
                profIssueModel.addRow(new Object[]{p.getRoomNo(),p.getPcNo(), status, info, p.getDate()});
            }
            for(Unit u : AVAILABLE) {
                    profAvailModel.addRow(new Object[]{u.getRoomNo(),u.getPcNo(), "Available", u.getDate()});
            
            }
        }

        private void refreshAdminTable() {
            adminModel.setRowCount(0);
            Collections.sort(MASTERLIST);
            for(Unit u : MASTERLIST) {
                String status = "Available";
                String info = "-";
                if(u.isRepair()) { status = "Under Repair"; }
                else if(u.isError()) { status = "Error"; info = u.getErrorCode(); }
                else if(u.isReported()) { status = "Reported"; info = u.getErrorClaim(); }
                adminModel.addRow(new Object[]{u.getRoomNo(), u.getPcNo(), status, info, u.getDate()});
            }
        }

        private void saveAndRefresh() {
            try { DATA.saveData(); } catch(IOException e) { e.printStackTrace(); }
            if(studentTable.isShowing()) refreshStudentTable();
            if(profAvailTable.isShowing() || profIssueTable.isShowing()) refreshProfTables();
            if(adminTable.isShowing()) refreshAdminTable();
        }
        
        //BINARY SEARCH
        private Unit findUnit(int r, int p) {
        	int index = DATA.find(MASTERLIST, r, p);
        	//FIND FUNCTION
        	if (index!=-1) return MASTERLIST.get(index);
            return null;
        }
    }
}