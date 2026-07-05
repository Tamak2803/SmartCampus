#!/bin/bash
echo "======================================================================"
echo "[SETUP] Pre-populating 10 eligible test student profiles..."
echo "======================================================================"

# Create students STU1001 through STU1010 in student_db
for i in {1..10}
do
   curl -s -o /dev/null -X POST http://localhost:8081/api/students \
     -H "Content-Type: application/json" \
     -d "{\"id\":\"STU100${i}\",\"name\":\"Test Student ${i}\",\"programme\":\"Computer Science\",\"gpa\":3.8}"
done

echo "[SETUP COMPLETE] 10 student profiles created."
echo "======================================================================"
echo "[CONCURRENCY TEST] Dispatching 10 concurrent requests to enroll in CS101..."
echo "[SEAT CAPACITY LIMIT] CS101 maximum capacity is configured at 3 seats."
echo "======================================================================"

# Dispatch 10 parallel registration requests using background threads (&)
for i in {1..10}
do
   curl -s -w "Request for STU100${i} -> HTTP Status: %{http_code}\n" -X POST "http://localhost:8082/api/enrolments?studentId=STU100${i}&courseCode=CS101" &
done

# Wait for all background curl threads to finish executing
wait
echo "======================================================================"
echo "[TEST COMPLETED] Concurrency verification complete."
echo "======================================================================"
