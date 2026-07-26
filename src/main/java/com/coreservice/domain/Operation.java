package    com.coreservice.domain;
public class Operation {
    protected final OperationType type;
    protected OperationStatus status;

    public void setStatus(OperationStatus status) {
        this.status = status;
    }

    public Operation(OperationType type) {
        this.type = type;
    }

    public OperationType getType() {
        return type;
    }

    public OperationStatus getStatus() {
        return status;
    }
}