import { useForm } from "react-hook-form";
import { Modal, Button, Form } from "react-bootstrap";


export const AddModal = ({ show, handleClose, handleAddRepository }) => {
  const { register, handleSubmit } = useForm();

  const onSubmit = (data) => {
    handleAddRepository(data.name);
    handleClose();
  };

  return (
    <Modal show={show} onHide={handleClose} className="grid-modal">
      <Modal.Header closeButton>
        <Modal.Title>Add a new repository</Modal.Title>
      </Modal.Header>
      <Form onSubmit={handleSubmit(onSubmit)}>
        <Modal.Body>
          <Form.Group controlId="name">
            <Form.Label>
              <strong>
                Name<span style={{ color: "red" }}>*</span>
              </strong>
            </Form.Label>
            <Form.Control
              {...register("name", { required: true })}
            />
          </Form.Group>
        </Modal.Body>
        <Modal.Footer>
          <Button onClick={handleClose}>
            Cancel
          </Button>
          <Button type="submit">
            Confirm
          </Button>
        </Modal.Footer>
      </Form>
    </Modal>
  );
};