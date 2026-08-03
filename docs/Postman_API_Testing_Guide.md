# Postman API Testing Guide

Base URL: `http://localhost:8080`

All JSON requests require `Content-Type: application/json`. All routes except
`/api/auth/**` require a valid Bearer token because of `SecurityConfig`.

## Postman setup and JWT workflow

1. Create an environment named `Office Hours Local` with `baseUrl` set to
   `http://localhost:8080`.
2. Register a student and a professor with the requests below. Admins cannot
   self-register; seed an `ADMIN` user in MySQL with a BCrypt password hash.
3. Send `POST {{baseUrl}}/api/auth/login` for each role.
4. In that request's **Tests** tab, save the access token:

```javascript
pm.environment.set("studentToken", pm.response.json().accessToken);
```

   Use `professorToken` or `adminToken` for the other role logins.
5. For an authenticated request, choose **Authorization** > **Bearer Token**
   and set Token to `{{studentToken}}`, `{{professorToken}}`, or
   `{{adminToken}}` as required. Do not type `Bearer` in the field; Postman
   adds it.

The login token expires after 86,400 seconds by default. A missing, invalid,
or expired token is rejected with `401`; an authenticated user without the
required role receives `403`.

## Shared response shapes

Most domain errors use this shape (timestamp values vary):

```json
{
  "timestamp": "2026-08-04T10:15:30.000",
  "status": 409,
  "error": "CONFLICT",
  "message": "An account with this email already exists",
  "path": "/api/auth/register"
}
```

Validation failures return `400`. The application currently returns Spring's
combined validation message rather than a field-by-field error map.

## Authentication

### POST /api/auth/register

Authentication: Public. Registers only `STUDENT` or `PROFESSOR`; `ADMIN`
returns `403` because admins must be seeded.

Student body (`fullName`, `email`, `password`, and `role` are validated;
student-specific `rollNo` and `yearOfStudy` are also enforced by the service):

```json
{
  "fullName": "Aarav Mehta",
  "email": "aarav.mehta@college.edu",
  "password": "SecurePass123!",
  "role": "STUDENT",
  "rollNo": "CE-2023-045",
  "yearOfStudy": 3
}
```

Professor body (`department` is required by the service):

```json
{
  "fullName": "Dr. Priya Sharma",
  "email": "priya.sharma@college.edu",
  "password": "SecurePass123!",
  "role": "PROFESSOR",
  "department": "Computer Engineering"
}
```

Expected: `201 Created`.

```json
{
  "id": 101,
  "fullName": "Aarav Mehta",
  "email": "aarav.mehta@college.edu",
  "role": "STUDENT",
  "isActive": true,
  "createdAt": "2026-08-04T10:15:30"
}
```

Common failures: `400` for blank/invalid email, a password shorter than eight
characters, or missing role/profile fields; `409` for duplicate email.

### POST /api/auth/login

Authentication: Public.

```json
{
  "email": "aarav.mehta@college.edu",
  "password": "SecurePass123!"
}
```

Expected: `200 OK`.

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": 101,
    "fullName": "Aarav Mehta",
    "email": "aarav.mehta@college.edu",
    "role": "STUDENT",
    "isActive": true,
    "createdAt": "2026-08-04T10:15:30"
  }
}
```

Failures: `400` for an empty/invalid request, `401` for bad credentials, and
`403` for a deactivated account.

### GET /api/auth/me

Authentication: Requires any valid Bearer token.

No body. Expected: `200 OK`, with the same `UserResponse` shape as registration.

## Professor and slot routes

### GET /api/professors

Authentication: Requires any valid Bearer token.

No body. Optional query parameters: `department`, `name`, `page` (zero-based),
and `size`. Example: `GET {{baseUrl}}/api/professors?department=Computer%20Engineering&name=sharma&page=0&size=10`.

Expected: `200 OK` (Spring `Page` response).

```json
{
  "content": [{
    "professorId": 102,
    "fullName": "Dr. Priya Sharma",
    "department": "Computer Engineering",
    "officeLocation": null,
    "bio": null
  }],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

### GET /api/professors/{professorId}/slots

Authentication: Requires any valid Bearer token.

No body. Example: `GET {{baseUrl}}/api/professors/102/slots`.

Expected: `200 OK`. Only future/current `OPEN` and `FULL` slots are returned.

```json
{
  "professorId": 102,
  "professorName": "Dr. Priya Sharma",
  "slots": [{
    "slotId": 201,
    "professorId": 102,
    "slotDate": "2026-08-10",
    "startTime": "10:00:00",
    "endTime": "10:30:00",
    "capacity": 2,
    "bookedCount": 0,
    "status": "OPEN"
  }]
}
```

`404` is returned when the professor does not exist.

### GET /api/professors/slots/me

Authentication: Requires a `PROFESSOR` Bearer token.

No body. Expected: `200 OK` with an array of `SlotResponse` objects, including
the fields shown in the preceding response.

### POST /api/professors/slots

Authentication: Requires a `PROFESSOR` Bearer token.

`slotDate` must be a future ISO date; all fields are required; `capacity` must
be at least 1; `endTime` must be later than `startTime`.

```json
{
  "slotDate": "2026-08-10",
  "startTime": "10:00:00",
  "endTime": "10:30:00",
  "capacity": 2
}
```

Expected: `201 Created`.

```json
{
  "slotId": 201,
  "professorId": 102,
  "slotDate": "2026-08-10",
  "startTime": "10:00:00",
  "endTime": "10:30:00",
  "capacity": 2,
  "bookedCount": 0,
  "status": "OPEN"
}
```

Failures: `400` for validation/time order and `409` for an overlapping slot
(same professor, date, and start time).

### PUT /api/professors/slots/{slotId}

Authentication: Requires the owning `PROFESSOR` Bearer token.

Body is the complete `SlotRequest` shape:

```json
{
  "slotDate": "2026-08-10",
  "startTime": "10:00:00",
  "endTime": "10:30:00",
  "capacity": 3
}
```

Expected: `200 OK` with the updated `SlotResponse`. It returns `403` when the
professor does not own the slot. If active bookings exist, date/time cannot be
changed and capacity cannot be lowered; violations return `409`.

### DELETE /api/professors/slots/{slotId}

Authentication: Requires the owning `PROFESSOR` Bearer token.

No body. Expected: `204 No Content`. The slot is soft-cancelled; it is not
physically deleted. Returns `403` for a slot owned by another professor and
`404` for a missing slot.

### GET /api/professors/slots/{slotId}/bookings

Authentication: Requires an owning `PROFESSOR` Bearer token or any `ADMIN`
Bearer token.

No body. Expected: `200 OK`.

```json
{
  "slotId": 201,
  "bookings": [{
    "bookingId": 501,
    "studentId": 101,
    "studentName": "Aarav Mehta",
    "rollNo": "CE-2023-045",
    "status": "BOOKED",
    "bookedAt": "2026-08-04T10:20:00"
  }],
  "waitlist": [{
    "waitlistId": 301,
    "studentId": 103,
    "studentName": "Riya Patel",
    "position": 1
  }]
}
```

## Student profile routes

### GET /api/students/me

Authentication: Requires a `STUDENT` Bearer token.

No body. Expected: `200 OK`.

```json
{
  "studentId": 101,
  "fullName": "Aarav Mehta",
  "rollNo": "CE-2023-045",
  "yearOfStudy": 3
}
```

### PUT /api/students/me

Authentication: Requires a `STUDENT` Bearer token.

`rollNo` is required, nonblank, and at most 50 characters. `yearOfStudy` is
optional but, if provided, must be 1 through 8.

```json
{
  "rollNo": "CE-2023-045",
  "yearOfStudy": 4
}
```

Expected: `200 OK` with the `StudentProfileResponse` shape above. Returns
`409` if another student already uses the roll number.

## Booking and waitlist routes

### POST /api/bookings

Authentication: Requires a `STUDENT` Bearer token.

`slotId` is required.

```json
{
  "slotId": 201
}
```

If space is available, expected: `201 Created`.

```json
{
  "bookingId": 501,
  "slotId": 201,
  "studentId": 101,
  "status": "BOOKED",
  "bookedAt": "2026-08-04T10:20:00",
  "slotDate": "2026-08-10",
  "startTime": "10:00:00",
  "endTime": "10:30:00",
  "professorName": "Dr. Priya Sharma"
}
```

If the slot is full, expected: `202 Accepted`.

```json
{
  "waitlistId": 301,
  "slotId": 201,
  "studentId": 101,
  "status": "WAITING",
  "position": 1,
  "createdAt": "2026-08-04T10:20:00"
}
```

Returns `409` for duplicate active booking, a cancelled/completed/past slot,
or an already-active waitlist entry; `404` for a missing slot.

### GET /api/bookings/me

Authentication: Requires a `STUDENT` Bearer token.

No body. Optional query parameter: `status=BOOKED`, `status=CANCELLED`, or
`status=COMPLETED`. Example: `GET {{baseUrl}}/api/bookings/me?status=BOOKED`.

Expected: `200 OK`.

```json
{
  "bookings": [{
    "bookingId": 501,
    "slotId": 201,
    "studentId": 101,
    "status": "BOOKED",
    "bookedAt": "2026-08-04T10:20:00",
    "slotDate": "2026-08-10",
    "startTime": "10:00:00",
    "endTime": "10:30:00",
    "professorName": "Dr. Priya Sharma"
  }],
  "waitlistEntries": []
}
```

### DELETE /api/bookings/{bookingId}

Authentication: Requires the booking owner's `STUDENT` Bearer token or any
`ADMIN` Bearer token.

No body. Expected: `204 No Content`. Cancelling a `BOOKED` reservation frees a
seat and promotes the earliest waiting student, if any. Returns `403` when a
student attempts to cancel someone else's booking, `404` when missing, and
`409` when it is no longer active.

### DELETE /api/bookings/waitlist/{waitlistId}

Authentication: Requires the waitlist owner's `STUDENT` Bearer token.

No body. Expected: `204 No Content`. Remaining `WAITING` entries are
re-numbered, but no promotion happens. Returns `403` for another student's
entry, `404` for a missing entry, and `409` if no longer active.

## Admin routes

All routes below require an `ADMIN` Bearer token and have no request body.

### GET /api/admin/users

Expected: `200 OK`.

```json
[
  {
    "id": 101,
    "fullName": "Aarav Mehta",
    "email": "aarav.mehta@college.edu",
    "role": "STUDENT",
    "isActive": true
  }
]
```

### PATCH /api/admin/users/{userId}/deactivate

Example: `PATCH {{baseUrl}}/api/admin/users/101/deactivate`.

Expected: `204 No Content`. Returns `404` for a missing user.

### PATCH /api/admin/users/{userId}/activate

Example: `PATCH {{baseUrl}}/api/admin/users/101/activate`.

Expected: `204 No Content`. Returns `404` for a missing user.

### GET /api/admin/reports

Expected: `200 OK`.

```json
{
  "totalUsers": 3,
  "totalProfessors": 1,
  "totalStudents": 2,
  "totalBookings": 1,
  "totalActiveBookings": 1,
  "totalWaitlisted": 0,
  "slotUtilizationPercent": 50.0
}
```

### DELETE /api/admin/bookings/{bookingId}

Example: `DELETE {{baseUrl}}/api/admin/bookings/501`.

Expected: `204 No Content`. This delegates to the same cancellation and
waitlist-promotion flow as the student cancellation endpoint. Returns `404`
for a missing booking or `409` for an inactive one.

## Recommended test order

1. Register and log in a professor; save `professorToken`.
2. Create a future slot; save its `slotId` as `slotId` in Postman.
3. Register and log in a student; save `studentToken`.
4. Use `GET /api/professors`, then `GET /api/professors/{{professorId}}/slots`.
5. Create a booking with `POST /api/bookings`.
6. Use a second student to create a waitlist entry once capacity is reached.
7. Cancel the first booking and confirm the second student's entry becomes a
   confirmed booking through `GET /api/bookings/me` or the professor roster.
