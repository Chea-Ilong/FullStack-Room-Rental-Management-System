package Exceptions;

public class RoomException extends Exception {
    public RoomException(String message) {
        super(message);
    }
}

//
//    // Validate if the room is occupied before performing certain operations
//    public static void validateRoomOccupied(Room room) throws RoomException {
//        if (!room.isOccupied()) {
//            throw new RoomException("Error: Room " + room.getRoomNumber() + " is not occupied.");
//        }
//    }
//
//    // Validate if the room is vacant before performing certain operations
//    public static void validateRoomVacant(Room room) throws RoomException {
//        if (room.isOccupied()) {
//            throw new RoomException("Error: Room " + room.getRoomNumber() + " is currently occupied.");
//        }
//    }
//
//    // Validate if the utility counters are greater than the current counters
//    public static void validateUtilityCounters(int newElectricCounter, int newWaterCounter, int currentElectricCounter, int currentWaterCounter) throws RoomException {
//        if (newElectricCounter < currentElectricCounter || newWaterCounter < currentWaterCounter) {
//            throw new RoomException("Error: New counters must be greater than the current counters.");
//        }
//    }
//
//    // Validate if the room number is not null or empty
//    public static void validateRoomNumber(String roomNumber) throws RoomException {
//        if (roomNumber == null || roomNumber.trim().isEmpty()) {
//            throw new RoomException("Error: Room number cannot be null or empty.");
//        }
//    }
//
//}