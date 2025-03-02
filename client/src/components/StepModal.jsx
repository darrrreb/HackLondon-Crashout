import { Modal, Button, Form } from "react-bootstrap";


export const StepModal = ({ node, show, handleClose }) => {

  return (
    <Modal show={show} onHide={handleClose}>
      <Modal.Header closeButton>
        <Modal.Title>Step details</Modal.Title>
      </Modal.Header>
        <Modal.Body>
          {node ? (
            <div style={{ overflowWrap: 'break-word', wordWrap: 'break-word' }}>
              <div style={{ marginBottom: '1em' }}>SHA: {node.data.sha}</div>
              <div style={{ marginBottom: '1em' }}>Short message: {node.data.label}</div>
              <div>Summary: {node.data.summary}</div>
            </div>
          ) : (
            <div>No node selected</div>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button onClick={handleClose}>
            Back
          </Button>
        </Modal.Footer>
    </Modal>
  );
};