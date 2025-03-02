import React from "react";
import { Container, Nav, Navbar, NavDropdown } from "react-bootstrap";
import { GearWideConnected } from "react-bootstrap-icons";

const Navigation = () => {

    return (
        <Navbar data-bs-theme="dark">
            <Container fluid>
                <Navbar.Brand href="/">&#62;&#62;&#62; HEAD Chef</Navbar.Brand>
                <Navbar.Toggle />
                <Navbar.Collapse className="justify-content-end">
                    <Nav>
                        <NavDropdown title="Settings" id="basic-nav-dropdown">
                            <NavDropdown.Item href="#action/3.1">Profile</NavDropdown.Item>
                            <NavDropdown.Item href="#action/3.2">
                                Options
                            </NavDropdown.Item>
                            <NavDropdown.Item href="#action/3.3">Connected Devices</NavDropdown.Item>
                            <NavDropdown.Divider />
                            <NavDropdown.Item href="#action/3.4">
                                Help
                            </NavDropdown.Item>
                        </NavDropdown>
                        <Nav.Link>
                            <GearWideConnected size={25} />
                        </Nav.Link>

                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
};

export default Navigation;