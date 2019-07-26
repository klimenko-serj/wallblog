import React from 'react';
import ReactDOM from "react-dom";
import Avatar from '@material-ui/core/Avatar';
import Button from '@material-ui/core/Button';
import CssBaseline from '@material-ui/core/CssBaseline';
import TextField from '@material-ui/core/TextField';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Checkbox from '@material-ui/core/Checkbox';
import Link from '@material-ui/core/Link';
import Grid from '@material-ui/core/Grid';
import Box from '@material-ui/core/Box';
import LockOutlinedIcon from '@material-ui/icons/LockOutlined';
import Typography from '@material-ui/core/Typography';
import { withStyles } from '@material-ui/core/styles';
import Container from '@material-ui/core/Container';
import LinearProgress from '@material-ui/core/LinearProgress';
import axios from 'axios'

const styles = theme => ({
  '@global': {
    body: {
      backgroundColor: theme.palette.common.white,
    },
  },
  paper: {
    marginTop: theme.spacing(8),
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
  },
  avatar: {
    margin: theme.spacing(1),
    backgroundColor: theme.palette.secondary.main,
  },
  form: {
    width: '100%', // Fix IE 11 issue.
    marginTop: theme.spacing(1),
  },
  submit: {
    margin: theme.spacing(3, 0, 2),
  },
});

export default withStyles(styles)(
    class SignIn extends React.Component {

        constructor(props){
            super(props)

            this.state = {inProgress: false}
        }
        
        doSignIn() {

        let usrn = document.getElementById("username").value
        let pwd  = document.getElementById("password").value
        
        if(!(usrn && pwd)){
          alert("Fill all required fields!")
          return;
        }

        this.setState({inProgress: true})
        
        axios.post('/api/signin', {
            username: usrn,
            password: pwd
          })
          .then(function (response) {
            let rs = response.status;
            if(rs == 200) {
              window.location.replace("/");
            } else {
              this.setState({inProgress:false})
              alert("Something went wrong...")
            }
          }.bind(this))
          .catch(function (error) {
            console.log(error);
            let rs = error.response.status;
            if(rs == 403) {
              this.setState({inProgress:false});
              alert("Wrong password!");
            } else if(rs == 404) {
              this.setState({inProgress:false})
              alert("There is no user with such name...")
            } else {
              this.setState({inProgress:false})
              alert("Something went wrong...")
            }
          }.bind(this));
        }

        render(){
            const { classes } = this.props;

            return (
                <Container component="main" maxWidth="xs">
                <CssBaseline />
                <div className={classes.paper}>
                    <Avatar className={classes.avatar}>
                    <LockOutlinedIcon />
                    </Avatar>
                    <Typography component="h1" variant="h5">
                    Sign in
                    </Typography>
                    <form className={classes.form} noValidate>
                    <TextField
                        variant="outlined"
                        margin="normal"
                        required
                        fullWidth
                        id="username"
                        label="Username"
                        name="username"
                        autoComplete="username"
                        autoFocus
                    />
                    <TextField
                        variant="outlined"
                        margin="normal"
                        required
                        fullWidth
                        name="password"
                        label="Password"
                        type="password"
                        id="password"
                        autoComplete="current-password"
                    />
                    { (!this.state.inProgress) ? 
                    <Button
                        type="button"
                        fullWidth
                        variant="contained"
                        color="primary"
                        className={classes.submit}
                        onClick={this.doSignIn.bind(this)}
                    >
                        Sign In
                    </Button>
                    :
                    <LinearProgress />
                    }
                    <Grid container>
                        <Grid item>
                        <Link href="/signup" variant="body2">
                            {"Don't have an account? Sign Up"}
                        </Link>
                        </Grid>
                    </Grid>
                    </form>
                </div>
                </Container>
            );
        }
    }
)