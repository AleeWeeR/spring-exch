# Repository Layer Architecture

## Overview

This implementation uses a proper **repository layer pattern** with clear separation of concerns:

- **Oracle Repository Layer**: Small, focused functions for data access
- **Java Repository Layer**: Calls individual Oracle functions
- **Java Service Layer**: Orchestrates business logic by calling multiple repository methods

This is different from the monolithic approach used in `Get_Charged_Info` where one large Oracle function does everything.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────┐
│  Controller Layer                                       │
│  WsIdStatusController                                   │
│  - Handles HTTP requests                                │
│  - Security & validation                                │
└───────────────────────┬─────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│  Service Layer (Business Logic Orchestration)           │
│  WsIdStatusServiceImpl                                  │
│  ┌─────────────────────────────────────────────────┐   │
│  │ checkStatus()                                    │   │
│  │  1. Call repository.isPersonActive()            │   │
│  │  2. If inactive:                                 │   │
│  │     - Call repository.getPersonCloseStatus()    │   │
│  │     - Call repository.checkCitizenArrival()     │   │
│  │     - Call repository.restoreArrivedPerson()    │   │
│  │  3. Call repository.logStatusRequest()          │   │
│  └─────────────────────────────────────────────────┘   │
└───────────────────────┬─────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│  Repository Layer (Data Access)                         │
│  WsIdStatusRepository.java                              │
│  ┌─────────────────────────────────────────────────┐   │
│  │ isPersonActive(pinfl)                           │   │
│  │ getPersonCloseStatus(pinfl)                     │   │
│  │ checkCitizenArrival(personId, pinfl, birthDate) │   │
│  │ restoreArrivedPerson(personId)                  │   │
│  │ logStatusRequest(...)                           │   │
│  │ getPersonIdByPinfl(pinfl)                       │   │
│  │ getPersonBirthDate(personId)                    │   │
│  └─────────────────────────────────────────────────┘   │
└───────────────────────┬─────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│  Oracle Database Layer                                  │
│  ┌──────────────────────┐  ┌─────────────────────────┐ │
│  │ Pf_Person_Repository │  │ Pf_Ws_Id_Status_Repo    │ │
│  │                      │  │                         │ │
│  │ - Is_Person_Active   │  │ - Check_Citizen_Arrival │ │
│  │ - Get_Person_By_Pinfl│  │ - Restore_Arrived_Person│ │
│  │ - Get_Close_Status   │  │ - Log_Status_Request    │ │
│  └──────────────────────┘  └─────────────────────────┘ │
│                  │                      │               │
│                  ▼                      ▼               │
│         ┌────────────────┐    ┌──────────────────┐     │
│         │ Pf_Persons     │    │ Pf_Person_Abroad │     │
│         │ Table          │    │ Package          │     │
│         └────────────────┘    └──────────────────┘     │
└─────────────────────────────────────────────────────────┘
```

## Benefits of This Approach

### 1. **Separation of Concerns**
- **Oracle functions**: Only handle data access (single responsibility)
- **Java service**: Handles business logic orchestration
- **Easy to test**: Can mock individual repository methods

### 2. **Reusability**
```java
// Oracle functions can be reused in other contexts
repository.isPersonActive(pinfl);  // Can be called from anywhere
repository.checkCitizenArrival(...);  // Reusable
```

### 3. **Better Error Handling**
```java
// Java service can handle each step independently
try {
    Integer status = repository.isPersonActive(pinfl);
    if (status == -1) {
        // Handle not found case specifically
    }
} catch (Exception e) {
    // Handle repository-specific errors
}
```

### 4. **Maintainability**
- Each Oracle function is small and focused
- Easy to modify one part without affecting others
- Clear call hierarchy

### 5. **Flexibility**
```java
// Service can make different decisions based on intermediate results
Map<String, Object> closeStatus = repository.getPersonCloseStatus(pinfl);
if ("11".equals(closeStatus.get("o_Close_Desc"))) {
    // Handle abroad case
} else {
    // Handle other close reasons
}
```

## Comparison: Monolithic vs Repository Layer

### Monolithic Approach (like Get_Charged_Info)

```
Controller → Service → ONE Oracle Function
                       │
                       ├─ Parse XML
                       ├─ Query tables
                       ├─ Business logic
                       ├─ Build JSON
                       └─ Log request

All in one function!
```

**Problems:**
- Hard to test individual parts
- Can't reuse components
- Complex error handling
- Difficult to modify

### Repository Layer Approach (This Implementation)

```
Controller → Service → Multiple Repository Methods
                       │
                       ├─ repository.isPersonActive()
                       ├─ repository.getPersonCloseStatus()
                       ├─ repository.checkCitizenArrival()
                       ├─ repository.restoreArrivedPerson()
                       └─ repository.logStatusRequest()

Each with single responsibility!
```

**Benefits:**
- ✅ Easy to test (mock individual methods)
- ✅ Reusable components
- ✅ Clear error handling per step
- ✅ Easy to modify
- ✅ Follows SOLID principles

## Code Flow Example

```java
// Service orchestrates the business logic
public WsIdStatusResponseDto checkStatus(WsIdStatusRequestDto requestDto) {
    String pinfl = requestDto.getData().getPinfl();
    Long wsId = requestDto.getData().getWsId();

    // Step 1: Check active status
    Integer activeStatus = repository.isPersonActive(pinfl);

    if (activeStatus == -1) {
        // Not found - return immediately
        return buildResponse(0, "Not found", wsId, null);
    }

    if (activeStatus == 1) {
        // Active - return immediately
        return buildResponse(1, "", wsId, 1);
    }

    // Step 2: Person inactive - check close status
    Map<String, Object> closeStatus = repository.getPersonCloseStatus(pinfl);
    String closeDesc = (String) closeStatus.get("o_Close_Desc");

    // Step 3: If abroad, check arrival
    if ("11".equals(closeDesc)) {
        Long personId = repository.getPersonIdByPinfl(pinfl);

        // Step 4: Check citizen arrival
        Map<String, Object> arrival = repository.checkCitizenArrival(personId, ...);

        if ((Integer) arrival.get("RETURN") == 1) {
            // Step 5: Restore person
            repository.restoreArrivedPerson(personId);
            return buildResponse(2, "Restored", wsId, null);
        }
    }

    return buildResponse(3, "Not arrived", wsId, 0);
}
```

## Oracle Repository Functions

### Pf_Person_Repository Package

**Purpose**: Data access for person-related queries

```sql
-- Check if person exists and is active
Is_Person_Active(p_Pinfl) RETURN NUMBER
  Returns: -1=not found, 0=inactive, 1=active

-- Get person record
Get_Person_By_Pinfl(p_Pinfl) RETURN Pf_Persons%ROWTYPE

-- Get closure status
Get_Person_Close_Status(p_Pinfl, o_Close_Reason, o_Close_Date, o_Close_Desc)
  Returns: 1=found, 0=not found
```

### Pf_Ws_Id_Status_Repository Package

**Purpose**: Business operations for WS ID status checks

```sql
-- Check if citizen has arrived (wraps existing function)
Check_Citizen_Arrival(p_Person_Id, p_Pinfl, p_Birth_Date, o_Message) RETURN NUMBER
  Returns: 1=arrived, 0=not arrived

-- Restore person who arrived (wraps existing function)
Restore_Arrived_Person(p_Person_Id, o_Message) RETURN NUMBER
  Returns: 1=success, 0=failed

-- Log status request
Log_Status_Request(p_Ws_Id, p_Pinfl, p_In_Data, p_Result_Code, p_Msg, p_Status)
```

## Installation

### 1. Install Oracle Repository Layer
```sql
sqlplus username/password@database
@database/PF_EXCHANGES_WS_ID_REPOSITORY_LAYER.sql
```

### 2. Grant Permissions
```sql
GRANT EXECUTE ON Pf_Person_Repository TO your_app_user;
GRANT EXECUTE ON Pf_Ws_Id_Status_Repository TO your_app_user;
```

### 3. Deploy Java Application
The Java code will automatically use the new repository layer.

## Testing

### Test Individual Repository Methods

```java
// Test isPersonActive
Integer status = repository.isPersonActive("41006673910061");
assertEquals(-1, status);  // Not found

// Test getPersonCloseStatus
Map<String, Object> closeStatus = repository.getPersonCloseStatus("41006673910061");
assertEquals("11", closeStatus.get("o_Close_Desc"));

// Test checkCitizenArrival
Map<String, Object> arrival = repository.checkCitizenArrival(123L, "41006673910061", date);
assertEquals(1, arrival.get("RETURN"));
```

### Mock Repository in Service Tests

```java
@Test
void testCheckStatus_PersonNotFound() {
    // Mock
    when(repository.isPersonActive(anyString())).thenReturn(-1);

    // Execute
    WsIdStatusResponseDto response = service.checkStatus(request);

    // Verify
    assertEquals(0, response.getResult());
    verify(repository, times(1)).isPersonActive(anyString());
    verify(repository, never()).checkCitizenArrival(any(), any(), any());
}
```

## Migration from Monolithic Approach

If you have existing monolithic functions like `Get_Charged_Info`, consider refactoring them to use this repository layer pattern:

1. **Identify separate concerns** in the monolithic function
2. **Create repository functions** for each concern
3. **Move business logic to Java service** layer
4. **Update Java repository** to call individual Oracle functions
5. **Update service** to orchestrate repository calls

This will make your code more maintainable, testable, and aligned with modern software architecture patterns.
