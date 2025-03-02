import { useState } from 'react';
import { PlusCircleFill } from "react-bootstrap-icons";
import { Button } from "react-bootstrap";
import plate_icon from '../assets/images/plate.png'
import { useNavigate } from "react-router-dom";
import { AddModal } from '../components/AddModal.jsx'

const LandingPage = () => {
	const [show, setShow] = useState(false);
	const navigate = useNavigate();

	const [repositories, setRepositories] = useState([
		{ id: 1, name: 'My hackathon project' },
		// Add more initial repositories if needed
	]);

	const handleShow = () => setShow(true);
	const handleClose = () => setShow(false);

	const handleNavigation = () => {
		navigate('/repo');
	};

	const handleAddRepository = (newRepoName) => {
		const newRepo = {
			id: repositories.length + 1,
			name: newRepoName,
		};
		setRepositories([...repositories, newRepo]);
		handleClose();
	};

	return (
		<>
			<div className="icon-heading-container">
				<PlusCircleFill className="icon" size={32} color={"white"} onClick={handleShow} />
				<h1 className="main-heading">Your repositories</h1>
			</div>
			{repositories.map((repo) => (
				<div key={repo.id} className="repository-item" onClick={() => handleNavigation(repo.id)}>
					<img
						alt="Repo Plate"
						src={plate_icon}
						width="30"
						height="30"
						className="d-inline-block"
					/>
					<Button>
						<h5 className="main-heading">{repo.name}</h5>
					</Button>
				</div>
			))}
			<div className="space">
			</div>
			<div>
				<h1 className="main-heading">Tutorials (Unreleased)</h1>
				<br/>
				<h5 className="main-heading">Getting started</h5>
				<h5 className="main-heading" onClick={() => navigate('/cli')} style={{ cursor: "pointer" }}>Command Line Interface</h5>
				<h5 className="main-heading">How does HEAD Chef work?</h5>
			</div>
			<AddModal show={show} handleClose={handleClose} handleAddRepository={handleAddRepository}/>
		</>
    );
};

export default LandingPage