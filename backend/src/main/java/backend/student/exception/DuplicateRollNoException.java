package backend.student.exception;

import backend.exception.ConflictException;
public class DuplicateRollNoException extends ConflictException { public DuplicateRollNoException(String rollNo) { super("Roll number already exists: " + rollNo); } }
